package cn.bugstack.ai.domain.agent.service;

import cn.bugstack.ai.domain.agent.model.valobj.prompt.PromptContextVO;

import java.util.List;
import java.util.Map;

/**
 * 上下文管理领域服务接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 */
public interface IChatContextService {
    PromptContextVO buildPromptContext(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory);
    List<Map<String, Object>> trimHistory(List<Map<String, Object>> history, int tokenBudget);
    void pushToolResult(String sessionId, String toolName, String result);
}
