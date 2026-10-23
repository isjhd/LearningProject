package cn.bugstack.ai.infrastructure.adapter.port;

import cn.bugstack.ai.domain.ssh.adapter.port.ITerminalSessionPort;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 终端会话管理器
 * 基础设施层实现，管理 Shell 通道的创建、读写、关闭
 *
 * @author waissh dev
 */
@Slf4j
@Component
public class TerminalSessionPort implements ITerminalSessionPort {

    @Resource
    private SshSessionPort sshSessionService;

    /** sessionId -> Shell 通道 */
    private final Map<String, ChannelShell> channels = new ConcurrentHashMap<>();

    /** sessionId -> 输出流 */
    private final Map<String, OutputStream> outputStreams = new ConcurrentHashMap<>();

    /** sessionId -> 输入流 */
    private final Map<String, InputStream> inputStreams = new ConcurrentHashMap<>();

    /** sessionId -> 未读输出缓冲区 */
    private final Map<String, StringBuilder> outputBuffers = new ConcurrentHashMap<>();

    /** sessionId -> Agent 专用缓冲区（不受前端轮询影响） */
    private final Map<String, StringBuilder> agentBuffers = new ConcurrentHashMap<>();

    /** sessionId -> 是否开启 Agent 捕获模式 */
    private final Map<String, Boolean> agentCaptureMode = new ConcurrentHashMap<>();

    /** sessionId -> 读取线程是否存活 */
    private final Map<String, Boolean> readerAlive = new ConcurrentHashMap<>();

    /** connectionId -> 当前活跃的 sessionId（一个连接只允许一个终端会话） */
    private final Map<String, String> activeConnectionSession = new ConcurrentHashMap<>();

    @Override
    public String openTerminal(String connectionId, int cols, int rows) {
        // 同一 connectionId 只允许一个终端会话，先关闭旧的
        String oldSessionId = activeConnectionSession.get(connectionId);
        if (oldSessionId != null) {
            log.info("关闭旧终端会话以避免重复 connectionId={} oldSessionId={}", connectionId, oldSessionId);
            cleanup(oldSessionId);
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "");

        try {
            Session session = sshSessionService.getSession(connectionId);
            if (session == null || !session.isConnected()) {
                throw new IllegalStateException("SSH会话不可用 connectionId=" + connectionId);
            }

            ChannelShell channel = (ChannelShell) session.openChannel("shell");
            channel.setPty(true);
            channel.setPtySize(cols, rows, 480, 640);

            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();

            channel.connect(5000);

            channels.put(sessionId, channel);
            inputStreams.put(sessionId, in);
            outputStreams.put(sessionId, out);
            outputBuffers.put(sessionId, new StringBuilder());
            agentBuffers.put(sessionId, new StringBuilder());
            agentCaptureMode.put(sessionId, false);
            activeConnectionSession.put(connectionId, sessionId);

            // 启动输出读取线程，持续读取 shell 输出到缓冲区
            startOutputReader(sessionId, in);

            // 等待 Shell 首次输出到达 + 额外等待让 MOTD 完整积累
            // 然后消费缓冲区，作为 initialOutput 返回给前端
            // 这样前端不再依赖轮询获取初始输出，彻底解决"有时显示有时不显示"的问题
            StringBuilder buffer = outputBuffers.get(sessionId);
            long waitDeadline = System.currentTimeMillis() + 3000; // 最多等 3s 等首数据
            try {
                // 阶段1：等首数据到达
                while (System.currentTimeMillis() < waitDeadline) {
                    synchronized (buffer) {
                        if (buffer.length() > 0) {
                            break;
                        }
                    }
                    Thread.sleep(30);
                }
                // 阶段2：额外等 200ms 让 MOTD/prompt 完整到达
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            log.info("终端会话打开成功 sessionId={} connectionId={}", sessionId, connectionId);
            return sessionId;

        } catch (Exception e) {
            log.error("打开终端会话失败 connectionId={}", connectionId, e);
            cleanup(sessionId);
            throw new RuntimeException("打开终端失败: " + e.getMessage(), e);
        }
    }

    /** 输入重试次数 */
    private static final int WRITE_MAX_RETRIES = 2;

    /** Write with retry on I/O failure */
    @Override
    public void write(String sessionId, String command) {
        OutputStream out = outputStreams.get(sessionId);
        if (out == null) {
            throw new IllegalArgumentException("终端会话不存在或已关闭 sessionId=" + sessionId);
        }

        IOException lastError = null;
        for (int attempt = 1; attempt <= WRITE_MAX_RETRIES; attempt++) {
            try {
                out.write(command.getBytes(StandardCharsets.UTF_8));
                out.flush();
                return; // 成功
            } catch (IOException e) {
                lastError = e;
                log.warn("写入终端失败 (attempt={}/{}) sessionId={} reason={}",
                        attempt, WRITE_MAX_RETRIES, sessionId, e.getMessage());
                // 最后一次重试后抛出异常
                if (attempt == WRITE_MAX_RETRIES) {
                    break;
                }
                // 短暂等待后重试
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.error("写入终端失败 (已重试 {} 次) sessionId={}", WRITE_MAX_RETRIES, sessionId, lastError);
        throw new RuntimeException("写入终端失败: " + lastError.getMessage(), lastError);
    }

    @Override
    public String read(String sessionId) {
        StringBuilder buffer = outputBuffers.get(sessionId);
        if (buffer == null) {
            throw new IllegalArgumentException("终端会话不存在或已关闭 sessionId=" + sessionId);
        }

        Boolean alive = readerAlive.get(sessionId);
        if (alive != null && !alive) {
            ChannelShell channel = channels.get(sessionId);
            if (channel == null || !channel.isConnected()) {
                return "\u001b[31m\r\n[连接已断开]\u001b[0m\r\n";
            }
            InputStream in = inputStreams.get(sessionId);
            if (in != null) {
                log.info("尝试重启终端读取线程 sessionId={}", sessionId);
                startOutputReader(sessionId, in);
            }
        }

        // 非阻塞：直接返回缓冲区当前内容，不等
        // 前端轮询本身就是等待机制，不需要后端再等
        synchronized (buffer) {
            if (buffer.length() == 0) {
                return "";
            }
            String output = buffer.toString();
            buffer.setLength(0);
            return output;
        }
    }

    @Override
    public void setAgentCapture(String sessionId, boolean capture) {
        StringBuilder agentBuffer = agentBuffers.get(sessionId);
        if (agentBuffer != null) {
            synchronized (agentBuffer) {
                // 开启捕获前先清空旧内容
                if (capture) {
                    agentBuffer.setLength(0);
                }
            }
        }
        agentCaptureMode.put(sessionId, capture);
        log.debug("Agent 捕获模式: sessionId={}, capture={}", sessionId, capture);
    }

    @Override
    public String readAgentBuffer(String sessionId) {
        StringBuilder agentBuffer = agentBuffers.get(sessionId);
        if (agentBuffer == null) {
            return "";
        }
        synchronized (agentBuffer) {
            if (agentBuffer.length() == 0) {
                return "";
            }
            String output = agentBuffer.toString();
            agentBuffer.setLength(0);
            return output;
        }
    }

    @Override
    public void resize(String sessionId, int cols, int rows) {
        ChannelShell channel = channels.get(sessionId);
        if (channel == null || !channel.isConnected()) {
            throw new IllegalArgumentException("终端会话不存在或已关闭 sessionId=" + sessionId);
        }

        try {
            channel.setPtySize(cols, rows, 480, 640);
            log.debug("终端大小已调整 sessionId={} {}x{}", sessionId, cols, rows);
        } catch (Exception e) {
            log.error("调整终端大小失败 sessionId={}", sessionId, e);
            throw new RuntimeException("调整终端大小失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void closeSession(String sessionId) {
        log.info("关闭终端会话 sessionId={}", sessionId);
        cleanup(sessionId);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        ChannelShell channel = channels.get(sessionId);
        return channel != null && channel.isConnected();
    }

    // ========== 内部方法 ==========

    /**
     * 启动输出读取线程
     * SocketTimeoutException 时继续循环（不是真正的断连），
     * 只有 EOF（-1）或真正的 IOException 才退出
     */
    private void startOutputReader(String sessionId, InputStream in) {
        readerAlive.put(sessionId, true);
        Thread reader = new Thread(() -> {
            byte[] buf = new byte[4096];
            int consecutiveErrors = 0;
            
            try {
                int len;
                while (true) {
                    try {
                        if ((len = in.read(buf)) == -1) {
                            // in.read() 返回 -1，说明 shell channel EOF
                            log.warn("终端 Shell Channel EOF sessionId={}", sessionId);
                            break;
                        }
                        
                        consecutiveErrors = 0; // 重置错误计数
                        
                        String text = new String(buf, 0, len, StandardCharsets.UTF_8);
                        StringBuilder buffer = outputBuffers.get(sessionId);
                        if (buffer != null) {
                            synchronized (buffer) {
                                buffer.append(text);
                            }
                        }
                        // 如果开启了 Agent 捕获模式，同时写入 agent 专用缓冲区
                        StringBuilder agentBuffer = agentBuffers.get(sessionId);
                        Boolean capture = agentCaptureMode.get(sessionId);
                        if (agentBuffer != null && Boolean.TRUE.equals(capture)) {
                            synchronized (agentBuffer) {
                                agentBuffer.append(text);
                            }
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        // SocketTimeout 不是断连，重试即可
                        log.debug("终端读取超时（非断连），继续读取 sessionId={}", sessionId);
                    } catch (IOException e) {
                        consecutiveErrors++;
                        
                        ChannelShell ch = channels.get(sessionId);
                        // channel 还活着说明可能是临时 I/O 问题，尝试恢复
                        if (ch != null && ch.isConnected() && consecutiveErrors < 3) {
                            log.warn("终端读取I/O异常({}/3)，继续尝试 sessionId={} reason={}", 
                                    consecutiveErrors, sessionId, e.getMessage());
                            Thread.sleep(100); // 短暂等待后重试
                            continue;
                        }
                        
                        // 超过重试次数或通道已断开
                        log.warn("终端输出读取异常 sessionId={} reason={} consecutiveErrors={}", 
                                sessionId, e.getMessage(), consecutiveErrors);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                log.info("终端读取线程被中断 sessionId={}", sessionId);
                Thread.currentThread().interrupt();
            } finally {
                readerAlive.put(sessionId, false);

                // 诊断：为什么线程退出了？
                ChannelShell ch = channels.get(sessionId);
                boolean channelConnected = ch != null && ch.isConnected();
                boolean channelClosed = ch != null && ch.isClosed();
                log.warn("终端输出读取线程退出 sessionId={} channelConnected={} channelClosed={}",
                        sessionId, channelConnected, channelClosed);

                // 通知等待中的 read() 方法：线程已退出，不再有数据
                StringBuilder buffer = outputBuffers.get(sessionId);
                if (buffer != null) {
                    synchronized (buffer) {
                        buffer.notifyAll();
                    }
                }
            }
        }, "terminal-reader-" + sessionId);
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * 清理资源
     */
    private void cleanup(String sessionId) {
        // 清理 connectionId -> sessionId 映射
        activeConnectionSession.entrySet().removeIf(e -> sessionId.equals(e.getValue()));

        try {
            OutputStream out = outputStreams.remove(sessionId);
            if (out != null) out.close();
        } catch (IOException ignored) {}

        try {
            InputStream in = inputStreams.remove(sessionId);
            if (in != null) in.close();
        } catch (IOException ignored) {}

        ChannelShell channel = channels.remove(sessionId);
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }

        outputBuffers.remove(sessionId);
        agentBuffers.remove(sessionId);
        agentCaptureMode.remove(sessionId);
        readerAlive.remove(sessionId);
    }

}
