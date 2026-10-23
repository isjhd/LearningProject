package cn.bugstack.ai.domain.agent.service.context;

import cn.bugstack.ai.domain.agent.model.valobj.prompt.MilestoneVO;
import cn.bugstack.ai.domain.agent.model.valobj.prompt.PromptContextVO;
import cn.bugstack.ai.domain.agent.service.IChatContextService;
import cn.bugstack.ai.domain.agent.service.context.provider.ContextProvider;
import cn.bugstack.ai.domain.agent.service.context.reducer.HybridReducer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bugstack.ai.domain.agent.service.context.provider.ToolResultProvider;

@Service
public class ChatContextService implements IChatContextService {
    private static final int DEFAULT_MAX_CONTEXT_TOKENS = 8000;

    // 多个实现类的集合，每个实现类负责提供一类上下文数据（如系统信息、用户信息、任务信息等），是上下文数据的来源。
    @Resource
    private List<ContextProvider> providers;

    // 专门用于裁剪超长的对话历史，适配大模型的 token 长度限制。
    @Resource
    private HybridReducer hybridReducer;

    // 负责存储 AI 调用外部工具（如命令执行、接口查询）后的返回结果。
    @Resource
    private ToolResultProvider toolResultProvider;

    @PostConstruct
    public void init() {
        providers.sort(Comparator.comparingInt(ContextProvider::getOrder));
    }

    // 构建 AI 提示词上下文
    @Override
    @SuppressWarnings("unchecked")
    public PromptContextVO buildPromptContext(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory) {
        Map<String, Object> finalCtx = new HashMap<>();

        for (ContextProvider provider : providers) {
            if (!provider.enabled()) continue;
            Map<String, Object> ctx = provider.provide(sessionId, userId, terminalSessionId, messageHistory);
            if (ctx != null) {
                finalCtx.putAll(ctx);
            }
        }

        return PromptContextVO.builder()
                .osInfo((String) finalCtx.get("osInfo"))
                .currentUser((String) finalCtx.get("currentUser"))
                .currentDirectory((String) finalCtx.get("currentDirectory"))
                .serverInfo((String) finalCtx.get("serverInfo"))
                .milestoneVOS((List<MilestoneVO>) finalCtx.get("milestoneVOS"))
                .toolResultSummary((String) finalCtx.get("toolResultSummary"))
                .taskDescription((String) finalCtx.get("taskDescription"))
                .build();
    }

    // 裁剪超长对话历史
    @Override
    public List<Map<String, Object>> trimHistory(List<Map<String, Object>> history, int tokenBudget) {
        if (history == null || history.isEmpty()) return Collections.emptyList();
        return hybridReducer.reduce(history, tokenBudget > 0 ? tokenBudget : DEFAULT_MAX_CONTEXT_TOKENS);
    }

    // 推送 / 存储工具执行结果
    @Override
    public void pushToolResult(String sessionId, String toolName, String result) {
        toolResultProvider.pushResult(sessionId, toolName, result);
    }
}
