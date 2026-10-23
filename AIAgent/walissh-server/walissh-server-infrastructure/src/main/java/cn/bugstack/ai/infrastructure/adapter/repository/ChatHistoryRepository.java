package cn.bugstack.ai.infrastructure.adapter.repository;

import cn.bugstack.ai.domain.agent.adapter.repository.IChatHistoryRepository;
import cn.bugstack.ai.domain.agent.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.agent.model.entity.ChatSessionEntity;
import cn.bugstack.ai.domain.agent.model.valobj.prompt.MilestoneVO;
import cn.bugstack.ai.infrastructure.dao.IChatMessageDao;
import cn.bugstack.ai.infrastructure.dao.IChatMilestoneDao;
import cn.bugstack.ai.infrastructure.dao.IChatSessionDao;
import cn.bugstack.ai.infrastructure.dao.po.ChatMessagePO;
import cn.bugstack.ai.infrastructure.dao.po.ChatMilestonePO;
import cn.bugstack.ai.infrastructure.dao.po.ChatSessionPO;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ChatHistoryRepository implements IChatHistoryRepository {

    @Resource
    private IChatSessionDao chatSessionDao;

    @Resource
    private IChatMessageDao chatMessageDao;

    @Resource
    private IChatMilestoneDao chatMilestoneDao;

    @Override
    public void saveSession(ChatSessionEntity session) {
        ChatSessionPO po = ChatSessionPO.builder()
                .id(session.getId())
                .agentId(session.getAgentId())
                .userId(session.getUserId())
                .title(session.getTitle())
                .messageCount(session.getMessageCount())
                .build();
        chatSessionDao.insert(po);
    }

    @Override
    public void saveMessage(ChatMessageEntity message) {
        ChatMessagePO po = ChatMessagePO.builder()
                .sessionId(message.getSessionId())
                .role(message.getRole())
                .content(message.getContent())
                .toolName(message.getToolName())
                .toolCallId(message.getToolCallId())
                .priority(message.getPriority())
                .tokenCount(message.getTokenCount())
                .build();
        chatMessageDao.insert(po);
        chatSessionDao.updateMessageCount(message.getSessionId());
    }

    @Override
    public List<ChatMessageEntity> getRecentMessages(String sessionId, int limit) {
        List<ChatMessagePO> pos = chatMessageDao.queryRecentBySessionId(sessionId, limit);
        if (pos == null || pos.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 数据库由于使用 ORDER BY id DESC，返回的是 [最新, 较旧, 最旧]
        // 为了 LLM 对话的正常顺序，我们需要将其反转为 [最旧, 较旧, 最新]
        List<ChatMessagePO> reversedPos = new java.util.ArrayList<>(pos);
        Collections.reverse(reversedPos);
        
        return reversedPos.stream().map(po -> ChatMessageEntity.builder()
                .id(po.getId())
                .sessionId(po.getSessionId())
                .role(po.getRole())
                .content(po.getContent())
                .toolName(po.getToolName())
                .toolCallId(po.getToolCallId())
                .priority(po.getPriority())
                .tokenCount(po.getTokenCount())
                .createdAt(po.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageEntity> getMessagesWithBudget(String sessionId, int tokenBudget) {
        // 查出最近的一定数量的消息
        List<ChatMessageEntity> recent = getRecentMessages(sessionId, 100);
        if (recent.isEmpty() || tokenBudget <= 0) {
            return recent;
        }

        List<ChatMessageEntity> result = new java.util.ArrayList<>();
        int currentTokens = 0;
        
        // 从最新消息往旧消息遍历（recent 列表现在是正序，所以从后往前）
        for (int i = recent.size() - 1; i >= 0; i--) {
            ChatMessageEntity msg = recent.get(i);
            int tokens = msg.getTokenCount() != null ? msg.getTokenCount() : 0;
            
            // 只要不是空结果，且加上当前消息的 token 后超过预算，就停止
            if (currentTokens + tokens > tokenBudget && !result.isEmpty()) {
                break;
            }
            
            result.add(0, msg); // 每次插到头部，保持正序
            currentTokens += tokens;
        }
        
        return result;
    }

    @Override
    public void saveMilestone(String sessionId, MilestoneVO milestoneVO) {
        ChatMilestonePO po = ChatMilestonePO.builder()
                .sessionId(sessionId)
                .type(milestoneVO.getType().name())
                .content(milestoneVO.getContent())
                .build();
        chatMilestoneDao.insert(po);
    }

    @Override
    public List<MilestoneVO> getRecentMilestones(String sessionId, int limit) {
        List<ChatMilestonePO> pos = chatMilestoneDao.queryRecentBySessionId(sessionId, limit);
        if (pos == null || pos.isEmpty()) {
            return Collections.emptyList();
        }
        return pos.stream().map(po -> MilestoneVO.builder()
                .type(MilestoneVO.Type.valueOf(po.getType()))
                .content(po.getContent())
                .timestamp(po.getCreatedAt() != null ? po.getCreatedAt().getTime() : System.currentTimeMillis())
                .build()).collect(Collectors.toList());
    }
}
