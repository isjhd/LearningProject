package cn.bugstack.ai.infrastructure.adapter.port;

import cn.bugstack.ai.domain.ssh.adapter.port.ISshFilePort;
import cn.bugstack.ai.domain.ssh.model.entity.SshFileContentEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshFileEntryEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshFileTreeEntity;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SshFilePort implements ISshFilePort {

    private static final int MAX_LIST_COUNT = 500;
    private static final int MAX_READ_BYTES = 512 * 1024;
    private static final int CHUNK_READ_BYTES = 256 * 1024;
    private static final String ROOT = "/";

    @Resource
    private SshSessionPort sshSessionPort;

    private final ConcurrentHashMap<String, ChannelSftp> sftpChannels = new ConcurrentHashMap<>();

    // ==================== 查询 ====================

    @Override
    public SshFileTreeEntity listDirectory(String connectionId, String path) {
        Session session = requireSession(connectionId);
        try {
            ChannelSftp sftp = getOrOpenSftp(connectionId, session);
            // 获取用户登录后的默认家目录，normalizePath：统一路径格式
            String homePath = normalizePath(sftp.pwd());
            // 如果前端传入路径，则使用前端传入的路径
            String currentPath = normalizePath(path == null || path.isBlank() ? homePath : path);
            // 获取父路径
            String parentPath = parentPathOf(currentPath);

            // 获取该目录下所有文件 / 文件夹的列表
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> list = sftp.ls(currentPath);
            List<SshFileEntryEntity> items = new ArrayList<>();

            for (ChannelSftp.LsEntry entry : list) {
                String name = entry.getFilename();
                // . = 当前目录   .. = 上级目录，这两个不需要展示，所以跳过
                if (".".equals(name) || "..".equals(name)) continue;
                // 判断是文件夹还是文件
                SftpATTRS attrs = entry.getAttrs();
                boolean directory = attrs != null && attrs.isDir();
                // 生成文件 / 文件夹的完整绝对路径
                String itemPath = ROOT.equals(currentPath) ? ROOT + name : currentPath + "/" + name;
                // 文件最后修改时间
                Long modifiedAt = attrs == null ? null : attrs.getMTime() * 1000L;
                // 文件大小
                Long size = attrs == null || directory ? null : attrs.getSize();

                // 把每个文件 / 文件夹都变成前端能直接用的对象。
                items.add(SshFileEntryEntity.builder()
                        .name(name).path(itemPath).directory(directory)
                        .modifiedAt(modifiedAt).size(size).build());
            }

            // 排序（文件夹优先，再按字母排序）
            items.sort(Comparator.comparing(SshFileEntryEntity::isDirectory).reversed()
                    .thenComparing(SshFileEntryEntity::getName, String.CASE_INSENSITIVE_ORDER));
            // 限制最多返回数量
            if (items.size() > MAX_LIST_COUNT) items = items.subList(0, MAX_LIST_COUNT);

            // 最终返回完整目录结构
            return SshFileTreeEntity.builder()
                    .rootPath(ROOT).homePath(homePath).currentPath(currentPath)
                    .parentPath(parentPath).items(items).build();
        } catch (SftpException e) {
            throw new IllegalArgumentException("目录不存在或无权限访问: " + path, e);
        } catch (Exception e) {
            throw new RuntimeException("查询目录失败", e);
        }
    }

    /** 基础读取（前 512KB），兼容旧接口 */
    @Override
    public SshFileContentEntity readFile(String connectionId, String path) {
        return readFile(connectionId, path, null, null);
    }

    /** 支持 offset + limit 的大文件分片读取 */
    @Override
    public SshFileContentEntity readFile(String connectionId, String path, Long offset, Integer limit) {
        // 根据 connectionId 获取已建立的 SSH 连接
        Session session = requireSession(connectionId);
        try {
            // 打开 / 复用 SFTP 通道（用来操作远程文件）
            ChannelSftp sftp = getOrOpenSftp(connectionId, session);
            // 把路径统一格式
            String normalizedPath = normalizePath(path);
            // 检查文件是否存在
            SftpATTRS attrs = sftp.stat(normalizedPath);
            // 检查不是文件夹
            if (attrs == null || attrs.isDir()) throw new IllegalArgumentException("目标不是文件: " + normalizedPath);

            // 文件总大小
            long fileSize = attrs.getSize();
            // 从第几字节开始读
            long readOffset = (offset != null && offset >= 0) ? offset : 0;
            // 最多读多少
            int readLimit = (limit != null && limit > 0) ? Math.min(limit, CHUNK_READ_BYTES) : CHUNK_READ_BYTES;
            // 防止读到文件末尾之外
            int readBytes = (int) Math.min(readLimit, fileSize - readOffset);
            // 剩下还有内容 = 被截断
            boolean truncated = readBytes < (fileSize - readOffset);

            // 从指定位置精确读取指定长度的字节
            byte[] contentBytes = readBytesWithOffset(sftp, normalizedPath, readOffset, readBytes);
            // 判断是不是二进制文件
            boolean binary = isLikelyBinary(contentBytes);
            String content = binary ? "" : new String(contentBytes, StandardCharsets.UTF_8);


            return SshFileContentEntity.builder()
                    .path(normalizedPath).name(fileNameOf(normalizedPath)).charset("UTF-8")
                    .size(fileSize).binary(binary).truncated(truncated)
                    .content(content).offset(readOffset).build();
        } catch (SftpException e) {
            throw new IllegalArgumentException("文件不存在或无权限访问: " + path, e);
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败", e);
        }
    }

    // ==================== 写操作：优先 SFTP，失败自动 sudo ====================

    @Override
    public void createFile(String connectionId, String path, boolean useSudo) {
        Session session = requireSession(connectionId);
        String normalizedPath = normalizePath(path);
        // 直接通过 shell 执行带 sudo 的 touch 命令创建文件
        if (useSudo) {
            // shellEscape()：利用单引号将路径圈起来，防止注入问题
            execSudo(session, "touch " + shellEscape(normalizedPath));
        } else {
            try {
                // 打开 / 复用 SFTP 通道
                ChannelSftp sftp = getOrOpenSftp(connectionId, session);
                // JSch 的 ChannelSftp 没有提供专门的 touch / create 空文件 API，
                // 上传空输入流是行业内标准的空文件创建方案，效果等同于 Linux 的 touch 命令
                try (InputStream in = new ByteArrayInputStream(new byte[0])) { sftp.put(in, normalizedPath); }
            } catch (Exception e) {
                if (isPermissionDenied(e)) {
                    log.warn("SFTP创建文件权限不足，自动尝试sudo: {}", normalizedPath);
                    execSudo(session, "touch " + shellEscape(normalizedPath));
                } else {
                    throw new IllegalArgumentException("创建文件失败: " + e.getMessage(), e);
                }
            }
        }
        log.info("创建文件: connectionId={}, path={}, sudo={}", connectionId, normalizedPath, useSudo);
    }

    @Override
    public void createDirectory(String connectionId, String path, boolean useSudo) {
        Session session = requireSession(connectionId);
        String normalizedPath = normalizePath(path);
        if (useSudo) {
            execSudo(session, "mkdir -p " + shellEscape(normalizedPath));
        } else {
            try {
                ChannelSftp sftp = getOrOpenSftp(connectionId, session);
                sftp.mkdir(normalizedPath);
            } catch (Exception e) {
                if (isPermissionDenied(e)) {
                    log.warn("SFTP创建目录权限不足，自动尝试sudo: {}", normalizedPath);
                    execSudo(session, "mkdir -p " + shellEscape(normalizedPath));
                } else {
                    throw new IllegalArgumentException("创建目录失败: " + e.getMessage(), e);
                }
            }
        }
        log.info("创建目录: connectionId={}, path={}, sudo={}", connectionId, normalizedPath, useSudo);
    }

    @Override
    public void rename(String connectionId, String oldPath, String newPath, boolean useSudo) {
        Session session = requireSession(connectionId);
        String nOld = normalizePath(oldPath);
        String nNew = normalizePath(newPath);
        if (useSudo) {
            execSudo(session, "mv " + shellEscape(nOld) + " " + shellEscape(nNew));
        } else {
            try {
                ChannelSftp sftp = getOrOpenSftp(connectionId, session);
                sftp.rename(nOld, nNew);
            } catch (Exception e) {
                if (isPermissionDenied(e)) {
                    log.warn("SFTP重命名权限不足，自动尝试sudo: {} -> {}", nOld, nNew);
                    execSudo(session, "mv " + shellEscape(nOld) + " " + shellEscape(nNew));
                } else {
                    throw new IllegalArgumentException("重命名失败: " + e.getMessage(), e);
                }
            }
        }
        log.info("重命名: connectionId={}, old={}, new={}, sudo={}", connectionId, nOld, nNew, useSudo);
    }

    @Override
    public void delete(String connectionId, String path, boolean useSudo) {
        Session session = requireSession(connectionId);
        String normalizedPath = normalizePath(path);
        if (useSudo) {
            execSudo(session, "rm -rf " + shellEscape(normalizedPath));
        } else {
            try {
                ChannelSftp sftp = getOrOpenSftp(connectionId, session);
                SftpATTRS attrs = sftp.stat(normalizedPath);
                if (attrs != null && attrs.isDir()) {
                    deleteDirectory(sftp, normalizedPath);
                } else {
                    sftp.rm(normalizedPath);
                }
            } catch (Exception e) {
                if (isPermissionDenied(e)) {
                    log.warn("SFTP删除权限不足，自动尝试sudo: {}", normalizedPath);
                    execSudo(session, "rm -rf " + shellEscape(normalizedPath));
                } else {
                    throw new IllegalArgumentException("删除失败: " + e.getMessage(), e);
                }
            }
        }
        log.info("删除: connectionId={}, path={}, sudo={}", connectionId, normalizedPath, useSudo);
    }

    @Override
    public void saveFile(String connectionId, String path, String content, boolean useSudo) {
        Session session = requireSession(connectionId);
        String normalizedPath = normalizePath(path);
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        if (useSudo) {
            execSudoWithInput(session, "sudo -S tee " + shellEscape(normalizedPath) + " > /dev/null", contentBytes);
        } else {
            try {
                ChannelSftp sftp = getOrOpenSftp(connectionId, session);
                try (InputStream in = new ByteArrayInputStream(contentBytes)) { sftp.put(in, normalizedPath); }
            } catch (Exception e) {
                if (isPermissionDenied(e)) {
                    log.warn("SFTP保存文件权限不足，自动尝试sudo: {}", normalizedPath);
                    execSudoWithInput(session, "sudo -S tee " + shellEscape(normalizedPath) + " > /dev/null", contentBytes);
                } else {
                    throw new IllegalArgumentException("保存文件失败: " + e.getMessage(), e);
                }
            }
        }
        log.info("保存文件: connectionId={}, path={}, sudo={}", connectionId, normalizedPath, useSudo);
    }

    @Override
    public void uploadFile(String connectionId, String path, InputStream inputStream) {
        Session session = requireSession(connectionId);
        String normalizedPath = normalizePath(path);
        try {
            ChannelSftp sftp = getOrOpenSftp(connectionId, session);
            sftp.put(inputStream, normalizedPath);
            log.info("上传文件: connectionId={}, path={}", connectionId, normalizedPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("上传文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void downloadFile(String connectionId, String path, OutputStream outputStream) {
        Session session = requireSession(connectionId);
        String normalizedPath = normalizePath(path);
        try {
            ChannelSftp sftp = getOrOpenSftp(connectionId, session);
            sftp.get(normalizedPath, outputStream);
            log.info("下载文件: connectionId={}, path={}", connectionId, normalizedPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("下载文件失败: " + e.getMessage(), e);
        }
    }

    // ==================== 内部工具 ====================

    public void closeSftp(String connectionId) {
        ChannelSftp sftp = sftpChannels.remove(connectionId);
        if (sftp != null) { try { if (sftp.isConnected()) sftp.disconnect(); } catch (Exception ignored) {} }
    }

    private Session requireSession(String connectionId) {
        Session session = sshSessionPort.getSession(connectionId);
        if (session == null || !session.isConnected()) { closeSftp(connectionId); throw new IllegalStateException("SSH连接未建立"); }
        // Session 存活但 SFTP channel 可能已断开，此时清理 channel，下次 getOrOpenSftp 会重建
        ChannelSftp existing = sftpChannels.get(connectionId);
        if (existing != null && (!existing.isConnected() || existing.isClosed())) {
            closeSftp(connectionId);
        }
        return session;
    }

    private synchronized ChannelSftp getOrOpenSftp(String connectionId, Session session) {
        ChannelSftp sftp = sftpChannels.get(connectionId);
        if (sftp != null && sftp.isConnected() && !sftp.isClosed()) return sftp;
        if (sftp != null) { try { sftp.disconnect(); } catch (Exception ignored) {} sftpChannels.remove(connectionId); }
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(8000);
            sftpChannels.put(connectionId, sftp);
            log.info("SFTP channel 已重建 connectionId={}", connectionId);
            return sftp;
        } catch (Exception e) {
            throw new RuntimeException("SFTP channel 打开失败: " + e.getMessage(), e);
        }
    }

    private void execSudo(Session session, String command) {
        execSudoWithInput(session, "sudo -S " + command, null);
    }

    private void execSudoWithInput(Session session, String command, byte[] stdinData) {
        ChannelExec channel = null;
        try {
            // 创建 exec 的 channel
            channel = (ChannelExec) session.openChannel("exec");
            // 设置命令
            channel.setCommand(command);
            if (stdinData != null) {
                channel.setInputStream(new ByteArrayInputStream(stdinData), false);
            }
            OutputStream out = channel.getOutputStream();
            // 连接通道
            channel.connect(8000);
            // sudo -S 从 stdin 读取密码，这里发一个空行（免密 sudo）
            out.write("\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            // 等待完成。整体命令执行总超时 10 秒，每 100ms 轮询一次通道状态。
            long deadline = System.currentTimeMillis() + 10000;
            while (!channel.isClosed() && System.currentTimeMillis() < deadline) { try { Thread.sleep(100); } catch (InterruptedException ignored) {} }
            if (!channel.isClosed()) { channel.disconnect(); throw new RuntimeException("sudo 命令超时"); }
            int exitCode = channel.getExitStatus();
            // 遵循 Linux 进程约定：退出码 0 表示成功，非 0 表示执行失败。
            if (exitCode != 0) {
                ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
                byte[] tmp = new byte[1024]; int n;
                while ((n = channel.getErrStream().read(tmp)) != -1) errBuf.write(tmp, 0, n);
                throw new RuntimeException("sudo 命令失败 (exit " + exitCode + "): " + errBuf.toString(StandardCharsets.UTF_8).trim());
            }
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException("sudo 执行异常: " + e.getMessage(), e); }
        finally { if (channel != null) channel.disconnect(); }
    }

    private static byte[] readBytes(ChannelSftp sftp, String path, int maxBytes) throws Exception {
        try (InputStream in = sftp.get(path); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192]; int total = 0, n;
            while ((n = in.read(buf)) != -1 && total < maxBytes) { int w = Math.min(maxBytes - total, n); out.write(buf, 0, w); total += w; }
            return out.toByteArray();
        }
    }


    /** 支持 offset + limit 的大文件分片读取 */
    private static byte[] readBytesWithOffset(ChannelSftp sftp, String path, long offset, int maxBytes) throws Exception {
        try (InputStream in = sftp.get(path)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (offset > 0) {
                // 尝试跳过 offset 字节
                long skipped = in.skip(offset);
                // 关键：某些 SFTP 库 skip 不可靠，可能没跳够
                if (skipped < offset) {
                    // skip 可能在某些 SFTP 实现上不完美，保守地读掉多余字节
                    byte[] tmp = new byte[(int) Math.min(8192, offset - skipped)];
                    int r;
                    while (skipped < offset && (r = in.read(tmp)) != -1) {
                        skipped += r;
                        if (skipped >= offset) break;
                    }
                }
            }
            byte[] buf = new byte[8192]; int total = 0, n;
            while ((n = in.read(buf)) != -1 && total < maxBytes) { int w = Math.min(maxBytes - total, n); out.write(buf, 0, w); total += w; }
            return out.toByteArray();
        }
    }

    private static boolean isLikelyBinary(byte[] bytes) {
        if (bytes.length == 0) return false;
        int sample = Math.min(bytes.length, 1024), nonText = 0;
        for (int i = 0; i < sample; i++) { int b = bytes[i] & 0xff; if (b == 0) return true; if (b < 0x09 || (b > 0x0d && b < 0x20)) nonText++; }
        return nonText > sample * 0.1;
    }

    private static boolean isPermissionDenied(Exception e) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains("permission denied");
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) return ROOT;
        String n = path.trim().replace("\\", "/");
        if (!n.startsWith(ROOT)) n = ROOT + n;
        while (n.contains("//")) n = n.replace("//", "/");
        if (n.length() > 1 && n.endsWith(ROOT)) n = n.substring(0, n.length() - 1);
        return n;
    }

    private static String parentPathOf(String path) {
        if (path == null || ROOT.equals(path)) return null;
        int idx = path.lastIndexOf('/');
        return idx <= 0 ? ROOT : path.substring(0, idx);
    }

    private static String fileNameOf(String path) {
        int idx = path.lastIndexOf('/');
        return (idx < 0 || idx == path.length() - 1) ? path : path.substring(idx + 1);
    }

    // 举例：
    // '/tmp/test; rm -rf /': /tmp/test; rm -rf /（分号不会被当成命令分隔符）
    // '/tmp/it'\''s file': /tmp/it's file（单引号被正确保留）
    private static String shellEscape(String path) { return "'" + path.replace("'", "'\\''") + "'"; }

    private void deleteDirectory(ChannelSftp sftp, String path) throws SftpException {
        @SuppressWarnings("unchecked") Vector<ChannelSftp.LsEntry> list = sftp.ls(path);
        for (ChannelSftp.LsEntry entry : list) {
            String name = entry.getFilename(); if (".".equals(name) || "..".equals(name)) continue;
            String itemPath = ROOT.equals(path) ? ROOT + name : path + "/" + name;
            if (entry.getAttrs().isDir()) deleteDirectory(sftp, itemPath); else sftp.rm(itemPath);
        }
        sftp.rmdir(path);
    }
}
