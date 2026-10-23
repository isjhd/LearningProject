package cn.bugstack.ai.domain.agent.service.context.reducer;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 组合「优先级筛选」和「滑动窗口筛选」两种策略，通过取交集的方式保留既重要又最新的消息。
// 同时强制兜底保留最新 2 条消息
@Component
public class HybridReducer implements MessageReducer {


    @Resource
    private PriorityReducer priorityReducer;// 优先级筛选器
    
    @Resource 
    private SlidingWindowReducer slidingReducer;// 滑动窗口筛选器

    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        // 1. 优先级策略筛选 → 转成【原始列表的索引集合】
        Set<Integer> priorityKeep = indexSet(priorityReducer.reduce(messages, tokenBudget), messages);
        // 2. 滑动窗口策略筛选 → 转成【原始列表的索引集合】
        Set<Integer> slidingKeep  = indexSet(slidingReducer.reduce(messages, tokenBudget), messages);

        // 取交集
        Set<Integer> keepIndices = new HashSet<>(priorityKeep);
        keepIndices.retainAll(slidingKeep);

        // 保证至少有最近 2 条
        int minKeep = Math.min(2, messages.size());
        for (int i = messages.size() - minKeep; i < messages.size(); i++) {
            keepIndices.add(i);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (keepIndices.contains(i)) result.add(messages.get(i));
        }
        return result;
    }

    private Set<Integer> indexSet(List<Map<String, Object>> subset, List<Map<String, Object>> all) {
        Set<Integer> indices = new HashSet<>();
        for (Map<String, Object> msg : subset) {
            int idx = all.indexOf(msg);
            if (idx >= 0) indices.add(idx);
        }
        return indices;
    }
}
