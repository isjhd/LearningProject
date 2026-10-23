package cn.bugstack.ai.domain.agent.service.intent;

import cn.bugstack.ai.domain.agent.model.valobj.intent.ConversationContextVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentHistoryEntryVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ContextTracker {

    // 上下文窗口大小：仅保留最近 10 条意图历史
    private static final int WINDOW_SIZE = 10;
    // 线程安全的Map，存储【会话ID -> 对话上下文】
    private final Map<String, ConversationContextVO> contexts = new ConcurrentHashMap<>();

    public ConversationContextVO getContext(String sessionId) {
        // 原子操作：会话不存在则创建，存在则直接返回
        return contexts.computeIfAbsent(sessionId, id -> ConversationContextVO.builder()
            .recentIntents(new LinkedList<>())// 链表存储意图历史
            .turnCount(0)// 对话轮次
            .sessionStartTime(System.currentTimeMillis())// 会话创建时间
            .build());
    }

    public void updateContext(String sessionId, IntentResultVO result) {
        ConversationContextVO ctx = getContext(sessionId);
        // 追加最新意图到链表尾部
        ctx.getRecentIntents().addLast(IntentHistoryEntryVO.builder()
            .intent(result.getIntent())
            .confidence(result.getConfidence())
            .timestamp(System.currentTimeMillis())
            .build());
        // 滑动窗口：超过10条，删除最早的意图
        if (ctx.getRecentIntents().size() > WINDOW_SIZE) {
            ctx.getRecentIntents().removeFirst();
        }
        // 更新对话轮次 + 最后一次意图
        ctx.setTurnCount(ctx.getTurnCount() + 1);
        ctx.setLastIntent(result.getIntent());
    }

    public void clear(String sessionId) {
        contexts.remove(sessionId);
    }
}
