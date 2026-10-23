package cn.bugstack.ai.domain.agent.service.prompt;

import cn.bugstack.ai.domain.agent.model.valobj.prompt.PromptContextVO;
import cn.bugstack.ai.domain.agent.service.IChatContextService;
import cn.bugstack.ai.domain.agent.service.IPromptService;
import cn.bugstack.ai.domain.agent.service.prompt.dynamic.DynamicPromptBuilder;
import cn.bugstack.ai.domain.agent.service.prompt.dynamic.MilestoneTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 提示词服务
 * <p>
 * 组合 DynamicPromptBuilder、MilestoneTracker、IChatContextService，
 * 向 case 层提供统一的提示词领域能力。
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/5/5 22:18
 */
@Slf4j
@Service
public class PromptService implements IPromptService {

    // 动态提示词构建器：生成提示词前缀
    @Resource
    private DynamicPromptBuilder dynamicPromptBuilder;

    // 里程碑追踪器：记录对话关键节点
    @Resource
    private MilestoneTracker milestoneTracker;

    // 聊天上下文服务：采集对话上下文数据
    @Resource
    private IChatContextService chatContextService;

    // 记录对话里程碑
    @Override
    public void detectAndRecordMilestone(String sessionId, String role, String content) {
        milestoneTracker.detectAndRecord(sessionId, role, content);
    }

    @Override
    public String buildEnrichedMessage(String userMessage, String sessionId, String terminalSessionId, List<String> recentCommands, List<Map<String, Object>> messageHistory) {
        // 1. 采集对话上下文
        PromptContextVO promptContextVO = chatContextService.buildPromptContext(sessionId, "userId_placeholder", terminalSessionId, messageHistory);

        // 2. 追加上层传入的最近命令
        promptContextVO.setRecentCommands(recentCommands);

        // 3. 生成动态提示词前缀
        String prefix = dynamicPromptBuilder.buildMessagePrefix(promptContextVO);

        // 4. 拼接前缀 + 原始用户消息
        if (prefix.isEmpty()) {
            return userMessage;
        }

        return prefix + "\n---\n" + userMessage;
    }

    @Override
    public void clearMilestones(String sessionId) {
        milestoneTracker.clear(sessionId);
    }
}
