package cn.bugstack.ai.domain.agent.service.armory.matter.tools;

import cn.bugstack.ai.domain.ssh.service.ISshTerminalService;
import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SSH 命令执行 ADK 工具
 * 为智能体提供在 SSH 终端执行命令的能力
 * 
 * 使用 ADK 的 @Schema 注解定义参数，支持 FunctionTool.create()
 * 
 * @author walissh dev
 */
@Slf4j
@Service
public class SshExecuteAdkTool {

    @Resource
    private ISshTerminalService sshTerminalService;

    // 会话ID -> 终端会话ID 映射（支持异步线程访问）
    private static final ConcurrentHashMap<String, String> sessionTerminalMapping = new ConcurrentHashMap<>();
    
    // 当前线程的终端会话 ID（使用 InheritableThreadLocal 支持异步线程继承）
    private static final InheritableThreadLocal<String> currentTerminalSession = new InheritableThreadLocal<>();

    // 危险命令模式（需要用户确认）
    // 通过正则表达式匹配命令中是否包含致命系统操作
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "\\b(rm\\s+-rf\\s+/|dd\\s+if=|mkfs\\.|:\\(\\)\\s*\\{|>\\s*/dev/sd|chmod\\s+-R\\s+777\\s+/)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 设置当前线程的终端会话 ID（兼容旧接口）
     */
    public static void setCurrentTerminalSession(String terminalSessionId) {
        currentTerminalSession.set(terminalSessionId);
        log.info("[ThreadLocal] 设置终端会话: thread={}, terminalSession={}", 
                Thread.currentThread().getName(), terminalSessionId);
    }

    /**
     * 清除当前线程的终端会话 ID
     */
    public static void clearCurrentTerminalSession() {
        currentTerminalSession.remove();
    }
    
    /**
     * 设置会话的终端会话 ID（基于 sessionId，支持异步）
     * @param sessionId 对话会话ID
     * @param terminalSessionId 终端会话ID
     */
    public static void setTerminalSession(String sessionId, String terminalSessionId) {
        if (sessionId != null && terminalSessionId != null) {
            sessionTerminalMapping.put(sessionId, terminalSessionId);
            log.info("[SessionMapping] 绑定终端会话: chatSession={}, terminalSession={}", sessionId, terminalSessionId);
        }
    }

    /**
     * 获取会话的终端会话 ID
     * @param sessionId 对话会话ID
     * @return 终端会话ID
     */
    public static String getTerminalSession(String sessionId) {
        return sessionId != null ? sessionTerminalMapping.get(sessionId) : null;
    }

    /**
     * 清除会话的终端会话绑定
     * @param sessionId 对话会话ID
     */
    public static void clearTerminalSession(String sessionId) {
        if (sessionId != null) {
            sessionTerminalMapping.remove(sessionId);
        }
    }

    /**
     * 在 SSH 终端执行命令
     * 
     * @param command 要执行的 Shell 命令
     * @return 执行结果
     */
    public Map<String, Object> executeCommand(
            @Schema(name = "command", description = "要执行的 Shell 命令，如: ls -la, apt install docker.io, docker --version")
            String command) {
        
        // 优先从 ThreadLocal 获取，支持异步线程继承
        String terminalSessionId = currentTerminalSession.get();
        
        log.info("[executeCommand] thread={}, terminalSessionId={}, command={}", 
                Thread.currentThread().getName(), terminalSessionId, command);
        
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            log.warn("[executeCommand] 终端会话ID为空，无法执行命令");
            return Map.of(
                    "success", false,
                    "output", "未绑定 SSH 终端会话。请先打开 SSH 终端连接。",
                    "command", command
            );
        }

        // 检查会话是否存在
        if (!sshTerminalService.sessionExists(terminalSessionId)) {
            log.warn("[executeCommand] 终端会话不存在: {}", terminalSessionId);
            return Map.of(
                    "success", false,
                    "output", "SSH 终端会话不存在或已关闭: " + terminalSessionId,
                    "command", command
            );
        }

        // 危险命令检测
        if (DANGEROUS_PATTERN.matcher(command).find()) {
            return Map.of(
                    "success", false,
                    "output", "⚠️ 危险命令被拦截: " + command + "\n该命令可能导致系统损坏或数据丢失。如确需执行，请手动在终端操作。",
                    "command", command
            );
        }

        try {
            log.info("SSH 执行命令: session={}, command={}", terminalSessionId, command);
            
            // 执行命令
            String output = sshTerminalService.executeCommand(terminalSessionId, command);
            
            log.info("SSH 命令执行完成: outputLength={}, output={}", 
                    output.length(), output.length() > 300 ? output.substring(0, 300) + "..." : output);
            
            // 分析输出，判断是否成功
            boolean success = isExecutionSuccessful(output);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("command", command);
            result.put("output", output);
            result.put("success", success);
            
            if (!success) {
                result.put("suggestion", analyzeError(output));
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("SSH 命令执行异常: session={}, command={}", terminalSessionId, command, e);
            return Map.of(
                    "success", false,
                    "output", "命令执行异常: " + e.getMessage(),
                    "command", command
            );
        }
    }

    /**
     * 检查 SSH 终端会话状态
     * 
     * @return 会话状态信息
     */
    public Map<String, Object> checkSession() {
        String terminalSessionId = currentTerminalSession.get();
        
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            return Map.of(
                    "exists", false,
                    "message", "未绑定 SSH 终端会话"
            );
        }
        
        boolean exists = sshTerminalService.sessionExists(terminalSessionId);
        return Map.of(
                "terminalSessionId", terminalSessionId,
                "exists", exists,
                "message", exists ? "SSH 终端会话正常" : "SSH 终端会话不存在或已关闭"
        );
    }

    /**
     * 列出可用的常用命令
     * 
     * @return 常用命令列表
     */
    public Map<String, Object> listCommonCommands() {
        return Map.of(
                "commands", java.util.List.of(
                        Map.of("command", "ls -la", "description", "列出当前目录所有文件（包含隐藏文件）"),
                        Map.of("command", "pwd", "description", "显示当前工作目录"),
                        Map.of("command", "df -h", "description", "显示磁盘使用情况"),
                        Map.of("command", "free -m", "description", "显示内存使用情况"),
                        Map.of("command", "docker ps", "description", "列出运行中的 Docker 容器"),
                        Map.of("command", "docker --version", "description", "检查 Docker 版本"),
                        Map.of("command", "systemctl status <service>", "description", "检查服务状态"),
                        Map.of("command", "cat /etc/os-release", "description", "显示操作系统信息")
                )
        );
    }

    /**
     * 判断命令执行是否成功
     */
    private boolean isExecutionSuccessful(String output) {
        if (output == null || output.isEmpty()) {
            return true;
        }
        
        String lowerOutput = output.toLowerCase();
        String[] errorIndicators = {
                "command not found", "no such file or directory", "permission denied",
                "operation not permitted", "cannot find", "error:", "failed",
                "fatal:", "unable to", "connection refused", "network is unreachable"
        };
        
        for (String indicator : errorIndicators) {
            if (lowerOutput.contains(indicator)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 分析错误并提供解决建议
     */
    private String analyzeError(String output) {
        if (output == null) return null;
        
        String lowerOutput = output.toLowerCase();
        
        if (lowerOutput.contains("command not found")) {
            return "命令不存在。可能原因：命令拼写错误、软件未安装、或命令不在 PATH 中。建议检查命令名称或安装对应软件包。";
        }
        if (lowerOutput.contains("permission denied")) {
            return "权限不足。建议使用 sudo 提升权限，或检查文件/目录权限。";
        }
        if (lowerOutput.contains("no such file or directory")) {
            return "文件或目录不存在。建议检查路径是否正确，或使用绝对路径。";
        }
        if (lowerOutput.contains("connection refused") || lowerOutput.contains("network is unreachable")) {
            return "网络连接问题。建议检查网络连接、确认目标服务是否运行、检查防火墙设置。";
        }
        return "执行失败，请检查命令和输出信息。";
    }
}
