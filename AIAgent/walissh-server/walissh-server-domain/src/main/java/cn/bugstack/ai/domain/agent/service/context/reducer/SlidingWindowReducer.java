package cn.bugstack.ai.domain.agent.service.context.reducer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SlidingWindowReducer implements MessageReducer {
    // 默认滑动窗口大小：最多保留 20 条最新消息
    private static final int DEFAULT_WINDOW_SIZE = 20;


    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        List<Map<String, Object>> window = new ArrayList<>();// 最终保留的窗口消息
        int usedTokens = 0;// 已使用的Token预算

        // 关键：从【最新的消息】开始，倒着往旧消息遍历
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> msg = messages.get(i);
            int msgTokens = estimateToken(msg);// 计算单条消息Token

            // 终止条件（满足任意一个就停止，不再保留旧消息）：
            // 1. 窗口已满20条  2. 再加这条就超Token预算
            if (window.size() >= DEFAULT_WINDOW_SIZE || usedTokens + msgTokens > tokenBudget) break;

            // 插入头部：保证最终列表是【原始时间正序】（最新的在最后）
            window.add(0, msg);
            usedTokens += msgTokens;
        }
        return window;
    }

    private int estimateToken(Map<String, Object> message) {
        String content = String.valueOf(message.get("content"));
        return content != null ? content.length() / 2 : 0;
    }
}
