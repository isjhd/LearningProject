package cn.bugstack.ai.domain.agent.service;

import cn.bugstack.ai.domain.agent.model.entity.ChatCommandEntity;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;

/**
 * 智能体聊天服务接口
 */
public interface IChatService {

    /**
     * 查询智能体配置列表
     */
    List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList();

    /**
     * 创建会话
     * @param agentId 智能体ID
     * @param userId 用户ID
     * @return sessionId 会话ID
     */
    String createSession(String agentId, String userId);

    /**
     * 处理消息
     * @param agentId 智能体ID
     * @param userId 用户ID
     * @param message 消息内容
     */
    List<String> handleMessage(String agentId, String userId, String message);

    /**
     * 处理消息（指定会话ID）
     * @param agentId 智能体ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param message 消息内容
     */
    List<String> handleMessage(String agentId, String userId, String sessionId, String message);

    /**
     * 处理消息（流式）
     * @param agentId 智能体ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param message 消息内容
     * @return 事件流
     */
    Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message);

    /**
     * 处理消息（流式）
     * @param agentId 智能体ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param message 消息内容
     * @param terminalSessionId SSH终端会话ID（用于MCP工具调用）
     * @return 事件流
     */
    Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message, String terminalSessionId);

    List<String> handleMessage(ChatCommandEntity chatCommandEntity);
}
