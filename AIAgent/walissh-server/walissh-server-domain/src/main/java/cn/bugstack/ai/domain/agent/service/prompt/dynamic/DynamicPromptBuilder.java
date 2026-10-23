package cn.bugstack.ai.domain.agent.service.prompt.dynamic;

import cn.bugstack.ai.domain.agent.model.valobj.prompt.MilestoneVO;
import cn.bugstack.ai.domain.agent.model.valobj.prompt.PromptContextVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 纯文本拼接工具，接收上下文数据，按照固定格式生成动态、结构化的 AI 提示词
@Slf4j
@Component
public class DynamicPromptBuilder {

    public String build(String baseInstruction, PromptContextVO ctx) {
        if (ctx == null) {
            return baseInstruction;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(baseInstruction);// 基础系统指令（比如：你是一个运维助手）

        appendEnvironmentInfo(sb, ctx);// 系统环境
        appendRecentCommands(sb, ctx);// 最近命令
        appendMilestones(sb, ctx);// 关键里程碑

        String result = sb.toString();
        log.debug("动态 Prompt 构建完成，长度: {} (基础: {}, 动态: {})",
                result.length(), baseInstruction.length(), result.length() - baseInstruction.length());
        return result;
    }

    /**
     * 将动态上下文构建为用户消息前缀（注入到用户消息中）
     * 适用于无法直接修改 system instruction 的场景
     */
    public String buildMessagePrefix(PromptContextVO ctx) {
        if (ctx == null) return "";

        StringBuilder sb = new StringBuilder();
        boolean hasContent = false;

        if (!isEmpty(ctx.getServerInfo()) || !isEmpty(ctx.getOsInfo())
                || !isEmpty(ctx.getCurrentUser()) || !isEmpty(ctx.getCurrentDirectory())) {
            sb.append("[系统环境]\n");
            if (!isEmpty(ctx.getServerInfo()))       sb.append("服务器: ").append(ctx.getServerInfo()).append("\n");
            if (!isEmpty(ctx.getOsInfo()))           sb.append("系统: ").append(ctx.getOsInfo()).append("\n");
            if (!isEmpty(ctx.getCurrentUser()))      sb.append("用户: ").append(ctx.getCurrentUser()).append("\n");
            if (!isEmpty(ctx.getCurrentDirectory())) sb.append("目录: ").append(ctx.getCurrentDirectory()).append("\n");
            hasContent = true;
        }

        if (ctx.getRecentCommands() != null && !ctx.getRecentCommands().isEmpty()) {
            sb.append("\n[最近执行的命令]\n");
            for (String cmd : ctx.getRecentCommands()) {
                sb.append("- ").append(cmd).append("\n");
            }
            hasContent = true;
        }

        if (ctx.getMilestoneVOS() != null && !ctx.getMilestoneVOS().isEmpty()) {
            sb.append("\n[关键事件]\n");
            for (MilestoneVO m : ctx.getMilestoneVOS()) {
                sb.append("- [").append(m.getType().name()).append("] ").append(m.getContent()).append("\n");
            }
            hasContent = true;
        }

        if (!isEmpty(ctx.getToolResultSummary())) {
            sb.append("\n[工具执行摘要]\n").append(ctx.getToolResultSummary()).append("\n");
            hasContent = true;
        }

        if (!isEmpty(ctx.getTaskDescription())) {
            sb.append("\n[当前任务]\n").append(ctx.getTaskDescription()).append("\n");
            hasContent = true;
        }

        if (!hasContent) return "";

        String prefix = sb.toString();
        log.debug("构建消息前缀，长度: {}", prefix.length());
        return prefix;
    }

    private void appendEnvironmentInfo(StringBuilder sb, PromptContextVO ctx) {
        if (isEmpty(ctx.getServerInfo()) && isEmpty(ctx.getOsInfo())
                && isEmpty(ctx.getCurrentUser()) && isEmpty(ctx.getCurrentDirectory())) {
            return;
        }
        sb.append("\n\n## 当前环境信息\n");
        if (!isEmpty(ctx.getServerInfo()))       sb.append("- 服务器: ").append(ctx.getServerInfo()).append("\n");
        if (!isEmpty(ctx.getOsInfo()))           sb.append("- 操作系统: ").append(ctx.getOsInfo()).append("\n");
        if (!isEmpty(ctx.getCurrentUser()))      sb.append("- 当前用户: ").append(ctx.getCurrentUser()).append("\n");
        if (!isEmpty(ctx.getCurrentDirectory())) sb.append("- 工作目录: ").append(ctx.getCurrentDirectory()).append("\n");
    }

    private void appendRecentCommands(StringBuilder sb, PromptContextVO ctx) {
        if (ctx.getRecentCommands() == null || ctx.getRecentCommands().isEmpty()) return;
        sb.append("\n## 最近操作记录\n");
        for (String cmd : ctx.getRecentCommands()) {
            sb.append("- ").append(cmd).append("\n");
        }
    }

    private void appendMilestones(StringBuilder sb, PromptContextVO ctx) {
        if (ctx.getMilestoneVOS() == null || ctx.getMilestoneVOS().isEmpty()) return;
        sb.append("\n## 关键事件\n");
        for (MilestoneVO m : ctx.getMilestoneVOS()) {
            sb.append("- [").append(m.getType().name()).append("] ").append(m.getContent()).append("\n");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
