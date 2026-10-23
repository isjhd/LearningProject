package cn.bugstack.ai.domain.agent.service.context.provider;

import java.util.List;
import java.util.Map;

/**
 * 上下文提供者接口
 */
public interface ContextProvider {
    String getName();
    int getOrder();
    boolean enabled();
    Map<String, Object> provide(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory);
}
