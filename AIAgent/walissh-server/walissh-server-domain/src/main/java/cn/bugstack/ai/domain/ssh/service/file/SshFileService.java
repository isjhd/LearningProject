package cn.bugstack.ai.domain.ssh.service.file;

import cn.bugstack.ai.domain.ssh.adapter.port.ISshFilePort;
import cn.bugstack.ai.domain.ssh.adapter.port.ISshSessionPort;
import cn.bugstack.ai.domain.ssh.model.entity.SshFileContentEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshFileTreeEntity;
import cn.bugstack.ai.domain.ssh.service.ISshFileDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 远程文件管理
@Slf4j
@Service
public class SshFileService implements ISshFileDomainService {

    // SSH 会话端口，仅用于校验连接是否有效
    private final ISshSessionPort sshSessionPort;
    // SSH 文件操作端口，实际执行远程文件 / 目录的底层操作。
    private final ISshFilePort sshFilePort;

    public SshFileService(ISshSessionPort sshSessionPort, ISshFilePort sshFilePort) {
        this.sshSessionPort = sshSessionPort;
        this.sshFilePort = sshFilePort;
    }

    // 获取远程服务器指定路径的目录树结构
    @Override
    public SshFileTreeEntity tree(String connectionId, String path) throws Exception {
        validateConnection(connectionId);
        return sshFilePort.listDirectory(connectionId, path);
    }

    // 全量读取远程文件的内容
    @Override
    public SshFileContentEntity content(String connectionId, String path) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        return sshFilePort.readFile(connectionId, path);
    }

    // 分页 / 按偏移量读取大文件内容
    @Override
    public SshFileContentEntity content(String connectionId, String path, Long offset, Integer limit) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        return sshFilePort.readFile(connectionId, path, offset, limit);
    }

    // 在远程服务器创建空文件，支持sudo管理员权
    @Override
    public void createFile(String connectionId, String path, boolean useSudo) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        sshFilePort.createFile(connectionId, path, useSudo);
    }

    // 在远程服务器创建目录，支持sudo权限。
    @Override
    public void createDirectory(String connectionId, String path, boolean useSudo) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        sshFilePort.createDirectory(connectionId, path, useSudo);
    }

    // 重命名 / 移动远程文件 / 目录，支持sudo权限。
    @Override
    public void rename(String connectionId, String oldPath, String newPath, boolean useSudo) throws Exception {
        validateConnection(connectionId);
        validatePath(oldPath);
        validatePath(newPath);
        sshFilePort.rename(connectionId, oldPath, newPath, useSudo);
    }

    // 删除远程文件或目录，支持sudo权限。
    @Override
    public void delete(String connectionId, String path, boolean useSudo) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        sshFilePort.delete(connectionId, path, useSudo);
    }

    // 将文本内容写入远程文件（覆盖 / 新建写入），支持sudo权限。
    @Override
    public void saveFile(String connectionId, String path, String content, boolean useSudo) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        sshFilePort.saveFile(connectionId, path, content, useSudo);
    }

    // 上传文件：将本地文件流（InputStream）上传到远程服务器指定路径；
    @Override
    public void uploadFile(String connectionId, String path, java.io.InputStream inputStream) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        sshFilePort.uploadFile(connectionId, path, inputStream);
    }

    // 下载文件：将远程服务器文件下载到本地输出流（OutputStream）。
    @Override
    public void downloadFile(String connectionId, String path, java.io.OutputStream outputStream) throws Exception {
        validateConnection(connectionId);
        validatePath(path);
        sshFilePort.downloadFile(connectionId, path, outputStream);
    }

    private void validateConnection(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            throw new IllegalArgumentException("连接ID不能为空");
        }
        if (!sshSessionPort.isConnected(connectionId)) {
            throw new IllegalStateException("SSH连接未建立，请先连接服务器");
        }
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("路径不能为空");
        }
    }
}
