package cn.bugstack.ai.domain.agent.service.context.reducer;

import java.util.List;
import java.util.Map;

/**
 * 消息裁剪器接口
 */
public interface MessageReducer {
    List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget);
}
