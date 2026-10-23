package cn.bugstack.ai.domain.agent.service.armory.matter.mcp.server;

import cn.bugstack.ai.domain.ssh.service.ISshTerminalService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.regex.Pattern;

/**
 * SSH 命令执行 MCP 工具
 * 为智能体提供在 SSH 终端执行命令的能力
 * 
 * @author waissh dev
 */
@Slf4j
@Service
public class SshExecuteMcpService {

    @Resource
    private ISshTerminalService sshTerminalService;

    // 当前会话绑定的终端会话 ID（通过 ThreadLocal 传递）
    // 使用 InheritableThreadLocal 支持异步线程继承
    private static final InheritableThreadLocal<String> currentTerminalSession = new InheritableThreadLocal<>();

    // 危险命令模式（需要用户确认）
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "\\b(rm\\s+-rf\\s+/|dd\\s+if=|mkfs\\.|:\\(\\)\\s*\\{|>\\s*/dev/sd|chmod\\s+-R\\s+777\\s+/)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 设置当前线程的终端会话 ID
     */
    public static void setCurrentTerminalSession(String terminalSessionId) {
        currentTerminalSession.set(terminalSessionId);
    }

    /**
     * 清除当前线程的终端会话 ID
     */
    public static void clearCurrentTerminalSession() {
        currentTerminalSession.remove();
    }

    /**
     * 在 SSH 终端执行命令
     */
    @Tool(description = """
            在 SSH 远程终端执行 Shell 命令并返回结果。
            
            适用场景：
            - 安装软件包（apt/yum/brew install）
            - 查看系统信息（cat /etc/os-release, uname -a）
            - 管理服务（systemctl start/stop/status）
            - 文件操作（ls, cat, grep, find）
            - 运行脚本或程序
            
            返回内容：
            - 命令的标准输出和错误输出
            - 执行状态（成功/失败）
            - 错误分析建议（如果失败）
            
            注意：
            - 危险命令（rm -rf /、dd 等）会被拦截
            - 长时间运行的命令建议使用 nohup
            """)
    public SshExecuteResponse executeCommand(SshExecuteRequest request) {
        String command = request.getCommand();
        String terminalSessionId = request.getTerminalSessionId();
        
        // 如果未指定终端会话，尝试从 ThreadLocal 获取
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            terminalSessionId = currentTerminalSession.get();
        }
        
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            return SshExecuteResponse.error(
                    "未绑定 SSH 终端会话。请先打开 SSH 终端或指定 terminalSessionId。"
            );
        }

        // 检查会话是否存在
        if (!sshTerminalService.sessionExists(terminalSessionId)) {
            return SshExecuteResponse.error(
                    "SSH 终端会话不存在或已关闭: " + terminalSessionId
            );
        }

        // 危险命令检测
        if (DANGEROUS_PATTERN.matcher(command).find()) {
            return SshExecuteResponse.error(
                    "⚠️ 危险命令被拦截: " + command + "\n" +
                    "该命令可能导致系统损坏或数据丢失。如确需执行，请手动在终端操作。"
            );
        }

        try {
            log.info("SSH 执行命令: session={}, command={}", terminalSessionId, command);
            
            // 执行命令
            String output = sshTerminalService.executeCommand(terminalSessionId, command);
            
            // 分析输出，判断是否成功
            boolean success = isExecutionSuccessful(output);
            
            SshExecuteResponse response = new SshExecuteResponse();
            response.setCommand(command);
            response.setOutput(output);
            response.setSuccess(success);
            
            if (!success) {
                response.setSuggestion(analyzeError(output));
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("SSH 命令执行异常: session={}, command={}", terminalSessionId, command, e);
            return SshExecuteResponse.error(
                    "命令执行异常: " + e.getMessage()
            );
        }
    }

    /**
     * 检查命令是否在指定终端会话中执行成功
     */
    @Tool(description = "检查 SSH 终端会话是否存在且可用")
    public CheckSessionResponse checkSession(CheckSessionRequest request) {
        String terminalSessionId = request.getTerminalSessionId();
        
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            terminalSessionId = currentTerminalSession.get();
        }
        
        CheckSessionResponse response = new CheckSessionResponse();
        response.setTerminalSessionId(terminalSessionId);
        
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            response.setExists(false);
            response.setMessage("未绑定 SSH 终端会话");
            return response;
        }
        
        boolean exists = sshTerminalService.sessionExists(terminalSessionId);
        response.setExists(exists);
        response.setMessage(exists ? "SSH 终端会话正常" : "SSH 终端会话不存在或已关闭");
        
        return response;
    }

    /**
     * 判断命令执行是否成功
     * 简单判断：如果没有明显的错误标识，认为成功
     */
    private boolean isExecutionSuccessful(String output) {
        if (output == null || output.isEmpty()) {
            return true; // 空输出通常表示成功
        }
        
        String lowerOutput = output.toLowerCase();
        
        // 常见错误标识
        String[] errorIndicators = {
                "command not found",
                "no such file or directory",
                "permission denied",
                "operation not permitted",
                "cannot find",
                "error:",
                "failed",
                "fatal:",
                "unable to",
                "connection refused",
                "network is unreachable"
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
            return "命令不存在。可能原因：\n" +
                   "1. 命令拼写错误\n" +
                   "2. 软件未安装\n" +
                   "3. 命令不在 PATH 环境变量中\n" +
                   "建议：检查命令名称或安装对应软件包";
        }
        
        if (lowerOutput.contains("permission denied")) {
            return "权限不足。建议：\n" +
                   "1. 使用 sudo 提升权限\n" +
                   "2. 检查文件/目录权限\n" +
                   "3. 确认当前用户是否有执行权限";
        }
        
        if (lowerOutput.contains("no such file or directory")) {
            return "文件或目录不存在。建议：\n" +
                   "1. 检查路径是否正确\n" +
                   "2. 使用绝对路径\n" +
                   "3. 先创建所需目录";
        }
        
        if (lowerOutput.contains("connection refused") || 
            lowerOutput.contains("network is unreachable")) {
            return "网络连接问题。建议：\n" +
                   "1. 检查网络连接\n" +
                   "2. 确认目标服务是否运行\n" +
                   "3. 检查防火墙设置";
        }
        
        return "执行失败，请检查命令和输出信息";
    }

    // ─── 请求/响应 DTO ─────────────────────────────────────────

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SshExecuteRequest {
        @JsonProperty(required = true, value = "command")
        @JsonPropertyDescription("要执行的 Shell 命令，如: ls -la, apt install docker.io")
        private String command;

        @JsonProperty(value = "terminalSessionId")
        @JsonPropertyDescription("SSH 终端会话 ID（可选，如未指定则使用当前绑定的会话）")
        private String terminalSessionId;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SshExecuteResponse {
        @JsonProperty(value = "command")
        @JsonPropertyDescription("执行的命令")
        private String command;

        @JsonProperty(value = "output")
        @JsonPropertyDescription("命令输出（包含 stdout 和 stderr）")
        private String output;

        @JsonProperty(value = "success")
        @JsonPropertyDescription("执行是否成功")
        private Boolean success;

        @JsonProperty(value = "suggestion")
        @JsonPropertyDescription("错误分析建议（仅在失败时提供）")
        private String suggestion;

        /**
         * 创建错误响应
         */
        public static SshExecuteResponse error(String message) {
            SshExecuteResponse response = new SshExecuteResponse();
            response.setSuccess(false);
            response.setOutput(message);
            return response;
        }
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CheckSessionRequest {
        @JsonProperty(value = "terminalSessionId")
        @JsonPropertyDescription("SSH 终端会话 ID（可选）")
        private String terminalSessionId;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CheckSessionResponse {
        @JsonProperty(value = "terminalSessionId")
        @JsonPropertyDescription("终端会话 ID")
        private String terminalSessionId;

        @JsonProperty(value = "exists")
        @JsonPropertyDescription("会话是否存在")
        private Boolean exists;

        @JsonProperty(value = "message")
        @JsonPropertyDescription("状态描述")
        private String message;
    }
}
