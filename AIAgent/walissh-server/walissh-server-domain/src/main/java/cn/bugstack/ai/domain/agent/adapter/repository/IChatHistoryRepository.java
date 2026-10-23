package cn.bugstack.ai.domain.agent.adapter.repository;

import cn.bugstack.ai.domain.agent.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.agent.model.entity.ChatSessionEntity;
import cn.bugstack.ai.domain.agent.model.valobj.prompt.MilestoneVO;

import java.util.List;

/**
 * 会话持久化仓储接口
 */
public interface IChatHistoryRepository {

    /**
     * 保存会话元数据
     */
    void saveSession(ChatSessionEntity session);

    /**
     * 保存对话消息
     */
    void saveMessage(ChatMessageEntity message);

    /**
     * 获取最近的消息
     */
    List<ChatMessageEntity> getRecentMessages(String sessionId, int limit);

    /**
     * 获取指定预算内的消息
     */
    List<ChatMessageEntity> getMessagesWithBudget(String sessionId, int tokenBudget);

    /**
     * 保存里程碑事件
     */
    void saveMilestone(String sessionId, MilestoneVO milestoneVO);

    /**
     * 获取最近的里程碑
     */
    List<MilestoneVO> getRecentMilestones(String sessionId, int limit);
}
