package cn.bugstack.ai.domain.agent.service.context.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class ToolResultProvider implements ContextProvider {

    // 存储每个会话的工具调用结果（会话ID -> 工具结果列表）
    private final Map<String, List<ToolResultEntry>> results = new ConcurrentHashMap<>();
    // 缓存每个会话的工具结果摘要（避免重复生成，提升性能）
    private final Map<String, String> summaryCache = new ConcurrentHashMap<>();

    @Override public String getName() { return "tool-result"; }
    @Override public int getOrder() { return 40; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory) {
        Map<String, Object> result = new HashMap<>();
        // 获取当前会话的工具结果
        List<ToolResultEntry> entries = results.getOrDefault(sessionId, Collections.emptyList());
        if (entries.isEmpty()) return result;

        // 懒摘要：有缓存直接返回，否则重新生成
        String summary = summaryCache.computeIfAbsent(sessionId, id -> generateSummary(entries));
        result.put("toolResultSummary", summary);
        return result;
    }

    public void pushResult(String sessionId, String toolName, String result) {
        results.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
               .add(new ToolResultEntry(toolName, result));
        summaryCache.remove(sessionId);  // 失效摘要缓存
    }

    private String generateSummary(List<ToolResultEntry> entries) {
        // 少量结果直接拼接，大量结果模板化压缩
        if (entries.size() <= 5) {
            return entries.stream()
                .map(e -> e.getToolName() + ": " + truncate(e.getResult(), 100))
                .collect(Collectors.joining("\n"));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("最近执行了 ").append(entries.size()).append(" 个工具调用:\n");
        // 只取最近 5 条详细 + 总结
        List<ToolResultEntry> recent = entries.subList(entries.size() - 5, entries.size());
        for (ToolResultEntry e : recent) {
            sb.append("- ").append(e.getToolName()).append(": ")
              .append(truncate(e.getResult(), 80)).append("\n");
        }
        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    @Data
    @AllArgsConstructor
    public static class ToolResultEntry {
        private String toolName;
        private String result;
    }
}
