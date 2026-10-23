package cn.bugstack.ai.domain.agent.service.context.provider;

import cn.bugstack.ai.domain.ssh.service.ISshTerminalService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 通过 SSH 连接远程终端，实时采集服务器 / 终端的核心运行环境信息
// 基础环境层：我在哪台机器？什么用户？什么目录？
@Component
public class TerminalStateProvider implements ContextProvider {

    // SSH终端服务：核心依赖，用于执行远程Shell命令
    @Resource
    private ISshTerminalService sshTerminalService;

    @Override public String getName() { return "terminal-state"; }
    @Override public int getOrder() { return 10; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory) {
        Map<String, Object> result = new HashMap<>();

        // 无终端会话ID，直接返回空（不执行命令）
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            return result;
        }

        // 执行4个核心Linux命令，获取环境信息
        String osInfo = safeExec(terminalSessionId, "uname -srm");
        String user   = safeExec(terminalSessionId, "whoami");
        String pwd    = safeExec(terminalSessionId, "pwd");
        String uptime = safeExec(terminalSessionId, "uptime -p 2>/dev/null || uptime");

        // 封装为固定Key的Map，与PromptContextVO严格对应
        result.put("osInfo", osInfo);
        result.put("currentUser", user);
        result.put("currentDirectory", pwd);
        result.put("uptime", uptime);
        return result;
    }

    private String safeExec(String terminalSessionId, String cmd) {
        try {
            String res = sshTerminalService.executeCommand(terminalSessionId, cmd);
            return res != null ? res.trim() : "";
        } catch (Exception e) { 
            return ""; 
        }
    }
}
