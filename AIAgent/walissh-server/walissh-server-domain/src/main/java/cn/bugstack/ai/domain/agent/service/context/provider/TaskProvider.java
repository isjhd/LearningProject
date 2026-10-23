package cn.bugstack.ai.domain.agent.service.context.provider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 自动抓取用户第一条提问作为官方任务描述
// 防止 AI 在多轮对话中偏离核心需求
// 任务目标层：用户要我做什么？
@Component
public class TaskProvider implements ContextProvider {
    @Override public String getName() { return "task"; }
    @Override public int getOrder() { return 20; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory) {
        Map<String, Object> result = new HashMap<>();
        
        if (messageHistory != null) {
            messageHistory.stream()
                // 过滤：只保留角色为 user 的消息
                .filter(m -> "user".equals(m.get("role")))
                // 取第一条用户消息
                .findFirst()
                // 如果存在，将消息内容存入 map，key 为 taskDescription
                .ifPresent(m -> result.put("taskDescription", m.get("content")));
        }
        
        return result;
    }
}
