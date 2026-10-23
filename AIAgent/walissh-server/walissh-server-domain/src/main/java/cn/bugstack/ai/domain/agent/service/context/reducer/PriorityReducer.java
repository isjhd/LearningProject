package cn.bugstack.ai.domain.agent.service.context.reducer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PriorityReducer implements MessageReducer {

    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        // 为每条消息推断优先级
        List<PrioritizedMessage> prioritized = messages.stream()
            .map(m -> new PrioritizedMessage(m, inferPriority(m)))
            .collect(Collectors.toList());

        // 至少保留最近 2 条
        int minKeep = Math.min(2, prioritized.size());
        List<PrioritizedMessage> kept = new ArrayList<>(prioritized.subList(
            prioritized.size() - minKeep, prioritized.size()));

        // 从低优先级开始丢弃，直到满足 token 预算
        int usedTokens = estimateTokens(kept);
        // 遍历：除了最新2条之外的「更早的消息」，从近到远遍历
        for (int i = prioritized.size() - minKeep - 1; i >= 0; i--) {
            PrioritizedMessage pm = prioritized.get(i);
            int msgTokens = estimateToken(pm.getMessage());
            if (usedTokens + msgTokens <= tokenBudget) {
                kept.add(0, pm);
                usedTokens += msgTokens;
            }
        }

        return kept.stream().map(PrioritizedMessage::getMessage).collect(Collectors.toList());
    }

    private Priority inferPriority(Map<String, Object> message) {
        String role = (String) message.get("role");// 角色
        String content = String.valueOf(message.get("content"));// 消息内容

        // 🔴 最高优先级：工具返回的错误消息
        if ("tool".equals(role) && containsAny(content, "error", "failed", "exception", "permission denied")) {
            return Priority.CRITICAL;
        }
        // 🟠 高优先级：用户发了配置文件/路径、系统消息
        if ("user".equals(role) && containsAny(content, "/", ".conf", ".yml", ".properties")) {
            return Priority.HIGH;
        }
        // 🟠 高优先级：用户发了配置文件/路径、系统消息
        if ("system".equals(role)) {
            return Priority.HIGH;
        }
        // 🟢 低优先级：助手超长消息（>5000字符，冗余内容）
        if ("assistant".equals(role) && content.length() > 5000) {
            return Priority.LOW;
        }
        // 🟡 默认：中等优先级
        return Priority.MEDIUM;
    }

    private boolean containsAny(String content, String... keywords) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    private int estimateToken(Map<String, Object> message) {
        String content = String.valueOf(message.get("content"));
        // 粗略估算：每 2 个字符 1 个 token
        return content != null ? content.length() / 2 : 0;
    }

    private int estimateTokens(List<PrioritizedMessage> messages) {
        return messages.stream().mapToInt(m -> estimateToken(m.getMessage())).sum();
    }

    @Data
    @AllArgsConstructor
    private static class PrioritizedMessage {
        private Map<String, Object> message;
        private Priority priority;
    }

    enum Priority { CRITICAL, HIGH, MEDIUM, LOW }
}
