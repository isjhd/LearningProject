package cn.bugstack.ai.cases.react.node;

import cn.bugstack.ai.api.dto.ChatRequestDTO;
import cn.bugstack.ai.api.dto.ReActResultDTO;
import cn.bugstack.ai.cases.react.AbstractAIAgentReActSupport;
import cn.bugstack.ai.cases.react.factory.DefaultReActFactory;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.bugstack.ai.domain.agent.service.IPromptService;
import cn.bugstack.ai.domain.agent.service.IChatContextService;
import cn.bugstack.ai.domain.agent.service.IIntentService;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;
import cn.bugstack.ai.domain.agent.adapter.repository.IChatHistoryRepository;
import cn.bugstack.ai.domain.agent.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.ai.domain.agent.service.armory.matter.mcp.server.SshExecuteMcpService;
import cn.bugstack.ai.domain.agent.service.armory.matter.tools.SshExecuteAdkTool;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.events.EventActions;
import com.google.adk.runner.Runner;
import com.google.genai.types.Content;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * AI 调用节点（ReAct 循环核心）
 *
 * <p>职责：
 * 1. 调用 ADK runner.runAsync() 获取事件流
 * 2. 处理文本内容，发送 SSE 事件
 * 3. 从 event.actions().stateDelta() 检测工具执行结果
 * 4. 如果有工具调用：存储到上下文，发送 SSE 事件，路由到 ToolCallNode
 * 5. 如果无工具调用：路由到 LoopDecisionNode
 *
 * <p>核心修复：
 * SpringAI 的 ChatModel.call() 自动执行工具，导致 event.functionCalls() 永远为空。
 * 修复方案：从 event.actions().stateDelta() 检测工具执行结果。
 * stateDelta 包含工具输出（key = output-key, value = 执行结果）。
 *
 * <p>ReAct 循环流程：
 * <pre>
 * RootNode
 *   └→ AiCallNode（调用 ADK runner，解析事件）
 *         ├→ [stateDelta 有结果] ToolCallNode → AiCallNode（循环）
 *         └→ [无工具调用] LoopDecisionNode → UserFeedbackNode
 * </pre>
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/5/4
 */
@Slf4j
@Component("reactAiCallNode")
public class AiCallNode extends AbstractAIAgentReActSupport {

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Resource
    private SshExecuteAdkTool sshExecuteAdkTool;

    @Resource
    private IPromptService promptService;
    
    @Resource
    private IChatContextService chatContextService;
    
    @Resource
    private IIntentService intentService;
    
    @Resource
    private IChatHistoryRepository chatHistoryRepository;

    /** SSE 事件发送间隔（字符数） */
    private static final int SSE_BATCH_SIZE = 20;

    /** tool name 映射：stateDelta key -> tool name */
    private static final Map<String, String> STATE_DELTA_TOOL_MAPPING = Map.of(
            "ssh_result", "executeCommand"
    );

    @Override
    protected ReActResultDTO doApply(ChatRequestDTO requestParameter, DefaultReActFactory.DynamicContext dynamicContext) throws Exception {
        log.info("ReAct AiCallNode - 开始 AI 调用，第 {} 步", dynamicContext.getStep() + 1);

        String agentId = dynamicContext.getAgentId();

        // 1. 获取 Agent 注册信息和 ADK Runner
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);
        if (aiAgentRegisterVO == null) {
            throw new RuntimeException("Agent not found: " + agentId);
        }

        Runner runner = aiAgentRegisterVO.getRunner();

        // 2. 获取最新用户消息
        String lastUserMessage = getLastUserMessage(requestParameter, dynamicContext);

        // [Phase 3] 意图识别
        IntentResultVO intentResult = intentService.classify(dynamicContext.getSessionId(), dynamicContext.getUserId(), lastUserMessage);
        log.info("识别到用户意图: {}, 置信度: {}", intentResult.getIntent().getLabel(), intentResult.getConfidence());
        
        // 将意图保存到上下文供后续使用
        dynamicContext.setCurrentIntent(intentResult.getIntent().name());

        // 3. 重置当前轮次缓冲
        dynamicContext.resetRoundBuffers();

        // [Phase 2] 裁剪消息历史
        List<Map<String, Object>> trimmedHistory = chatContextService.trimHistory(dynamicContext.getMessageHistory(), 8000);
        dynamicContext.setMessageHistory(new ArrayList<>(trimmedHistory));

        // 4. 绑定终端会话 ID
        String terminalSessionId = dynamicContext.getTerminalSessionId();
        if (terminalSessionId != null && !terminalSessionId.isEmpty()) {
            SshExecuteAdkTool.setCurrentTerminalSession(terminalSessionId);
            SshExecuteMcpService.setCurrentTerminalSession(terminalSessionId);
        }

        // 5. 构建动态上下文并注入用户消息
        String enrichedMessage = buildEnrichedMessage(lastUserMessage, dynamicContext);
        log.debug("注入动态上下文后消息长度: {} -> {}", lastUserMessage.length(), enrichedMessage.length());

        // [Phase 5] 保存用户消息到数据库（只在首轮保存原始消息）
        if (dynamicContext.getStep() == 0) {
            chatHistoryRepository.saveMessage(ChatMessageEntity.builder()
                    .sessionId(dynamicContext.getSessionId())
                    .role("user")
                    .content(lastUserMessage)
                    .priority("MEDIUM")
                    .tokenCount(lastUserMessage.length() / 2)
                    .build());
        }

        // 6. 构建用户消息
        com.google.genai.types.Content userContent = Content.builder()
                .role("user")
                .parts(com.google.genai.types.Part.builder().text(enrichedMessage).build())
                .build();

        // 7. 重置 ReAct 循环标志
        dynamicContext.setStopReason(null);
        dynamicContext.setErrorMessage(null);

        // 8. 调用 ADK Runner 并处理事件流
        ResponseBodyEmitter emitter = dynamicContext.getEmitter();
        StringBuilder textAccumulator = new StringBuilder();
        int roundToolCalls = 0;
        boolean hasError = false;
        StringBuilder errorBuilder = new StringBuilder();

        log.info("调用 ADK Runner，用户消息: {}", lastUserMessage.length() > 200
                ? lastUserMessage.substring(0, 200) + "..." : lastUserMessage);

        // 启动 Agent 异步执行 → 获取流式事件迭代器 → 逐个遍历事件 → 分别处理「文本内容」「工具执行结果」「消息历史」三类信息 → 实时通过 SSE 推送给前端 → 同步更新对话上下文。
        try {

            // 启动 Agent 并获取流式事件迭代器
            Iterator<Event> events = runner.runAsync(
                    dynamicContext.getUserId(),
                    dynamicContext.getSessionId(),
                    userContent,
                    RunConfig.builder().build()
            ).blockingIterable().iterator();// 将响应式流（Flux）转为阻塞迭代器，用于逐事件同步处理；

            int eventCount = 0;
            while (events.hasNext()) {
                Event event = events.next();
                eventCount++;

                event.stringifyContent();
                log.debug("处理第 {} 个事件: final={}, content_len={}",
                        eventCount,
                        event.finalResponse(),
                        event.stringifyContent().length());

                // 8.1 处理文本内容（模型的响应文本，包括工具调用后的总结）
                String eventText = event.stringifyContent();
                if (!eventText.isBlank()) {
                    // 非空则追加到 textAccumulator 累加器，更新上下文中的助手完整回复；
                    textAccumulator.append(eventText);
                    dynamicContext.setAssistantContent(textAccumulator);
                    sendTextEvent(emitter, eventText, textAccumulator.toString());
                }

                // 8.2 从 stateDelta 检测工具执行结果
                EventActions actions = event.actions();
                if (actions != null) {
                    // 消费这个 stateDelta，把每一条状态变更翻译为一次完整的 “工具调用 + 工具结果” 事件，存入上下文并通过 SSE 推给前端。
                    java.util.Map<String, Object> stateDelta = actions.stateDelta();
                    if (stateDelta != null && !stateDelta.isEmpty()) {
                        log.info("检测到 stateDelta 变更: keys={}", stateDelta.keySet());
                        // 遍历每一个状态变更项
                        for (Map.Entry<String, Object> entry : stateDelta.entrySet()) {
                            String stateKey = entry.getKey();
                            Object stateValue = entry.getValue();

                            // 跳过内部状态键（如 "REMOVED"）
                            if ("REMOVED".equals(stateValue)) {
                                continue;
                            }

                            // // 解析工具名、格式化结果、生成调用ID
                            String toolName = resolveToolName(stateKey);
                            String resultContent = formatStateValue(stateValue);
                            String toolCallId = "call_" + stateKey + "_" + System.currentTimeMillis();

                            log.info("工具执行结果: stateKey={}, toolName={}, result_length={}",
                                    stateKey, toolName, resultContent.length());

                            // 存储工具调用信息
                            Map<String, Object> toolCallInfo = new HashMap<>();
                            toolCallInfo.put("id", toolCallId);
                            toolCallInfo.put("name", toolName);
                            toolCallInfo.put("args", "");
                            dynamicContext.getCurrentToolCalls().add(toolCallInfo);

                            // 存储工具结果
                            Map<String, Object> toolResultInfo = new HashMap<>();
                            toolResultInfo.put("id", toolCallId);
                            toolResultInfo.put("name", toolName);
                            toolResultInfo.put("content", resultContent);
                            toolResultInfo.put("status", "success");
                            dynamicContext.getCurrentToolResults().add(toolResultInfo);

                            // 发送 SSE 工具调用事件
                            sendToolCallEvent(emitter, toolCallId, toolName, "executing");

                            // 发送 SSE 工具结果事件
                            sendToolResultEvent(emitter, toolCallId, resultContent, "success");

                            roundToolCalls++;
                            dynamicContext.incrementTotalToolCalls();

                            // 记录执行的命令到上下文
                            if ("executeCommand".equals(toolName) && !resultContent.isEmpty()) {
                                recordExecutedCommand(dynamicContext, resultContent);
                            }

                            // 记录里程碑（工具结果）
                            promptService.detectAndRecordMilestone(
                                    dynamicContext.getSessionId(), "tool", resultContent);
                            
                            // 记录到上下文提供者中
                            chatContextService.pushToolResult(dynamicContext.getSessionId(), toolName, resultContent);
                        }
                    }
                }

                // 8.3 记录 assistant 内容到消息历史
                if (event.content().isPresent()) {
                    Content content = event.content().get();
                    String role = content.role().orElse("assistant");
                    if ("assistant".equals(role)) {
                        String text = event.stringifyContent();
                        if (!text.isBlank()) {
                            dynamicContext.appendAssistantMessage(text);
                        }
                    }
                }
            }

            log.info("ADK Runner 事件流处理完成，共 {} 个事件", eventCount);

        } catch (Exception e) {
            log.error("ADK Runner 调用失败", e);
            hasError = true;
            errorBuilder.append("ADK Runner error: ").append(e.getMessage());
            dynamicContext.setErrorMessage(errorBuilder.toString());
            dynamicContext.setStopReason("error");
        } finally {
            // 清除终端会话绑定
            if (terminalSessionId != null && !terminalSessionId.isEmpty()) {
                SshExecuteAdkTool.clearCurrentTerminalSession();
                SshExecuteMcpService.clearCurrentTerminalSession();
            }
        }

        // 9. 更新步数和工具调用统计
        dynamicContext.incrementStep();
        dynamicContext.getResult().setTotalSteps(dynamicContext.getStep());
        dynamicContext.getResult().setTotalToolCalls(
                dynamicContext.getResult().getTotalToolCalls() + roundToolCalls
        );

        log.info("ReAct AiCallNode - 第 {} 步完成，本轮工具调用 {} 次，文本长度 {}",
                dynamicContext.getStep(), roundToolCalls, textAccumulator.length());

        // 10. 发送本轮结束事件
        sendRoundEndEvent(
                dynamicContext.getEmitter(),
                dynamicContext.getStep(),
                dynamicContext.getMaxSteps(),
                !hasError,
                dynamicContext.getResult().getTotalToolCalls()
        );

        // [Phase 5] 保存助手回复到数据库（如果是最终回复，或者包含实质性内容）
        if (textAccumulator.length() > 0) {
            chatHistoryRepository.saveMessage(ChatMessageEntity.builder()
                    .sessionId(dynamicContext.getSessionId())
                    .role("assistant")
                    .content(textAccumulator.toString())
                    .priority("MEDIUM")
                    .tokenCount(textAccumulator.length() / 2)
                    .build());
        }

        // 11. 错误处理
        if (hasError) {
            dynamicContext.setStopReason("error");
        }

        // 12. 路由
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ChatRequestDTO, DefaultReActFactory.DynamicContext, ReActResultDTO> get(
            ChatRequestDTO requestParameter,
            DefaultReActFactory.DynamicContext dynamicContext) throws Exception {

        // 检查是否应该终止
        String stopReason = dynamicContext.getStopReason();
        if (stopReason != null) {
            log.info("检测到终止条件: {}, 路由到 UserFeedbackNode", stopReason);
            return getBean("reactUserFeedbackNode");
        }

        // 检查是否达到最大步数
        if (dynamicContext.getStep() >= dynamicContext.getMaxSteps()) {
            log.info("达到最大步数 {}, 路由到 UserFeedbackNode", dynamicContext.getMaxSteps());
            dynamicContext.setStopReason("max_steps");
            return getBean("reactUserFeedbackNode");
        }

        // 检查本轮是否有工具调用（从 stateDelta 检测到的）
        if (!dynamicContext.getCurrentToolCalls().isEmpty()) {
            log.info("检测到 {} 个工具调用，路由到 ToolCallNode",
                    dynamicContext.getCurrentToolCalls().size());
            return getBean("reactToolCallNode");
        }

        // 无工具调用 → ReAct 循环完成
        log.info("无工具调用，ReAct 循环完成，路由到 LoopDecisionNode");
        return getBean("reactLoopDecisionNode");
    }

    // ═══════════════════════════════════════════════════════════════
    //  辅助方法
    // ═══════════════════════════════════════════════════════════════

    /**
     * 获取最新用户消息
     */
    private String getLastUserMessage(ChatRequestDTO requestParameter,
                                       DefaultReActFactory.DynamicContext dynamicContext) {
        if (requestParameter.getMessage() != null && !requestParameter.getMessage().isEmpty()) {
            return requestParameter.getMessage();
        }

        List<Map<String, Object>> history = dynamicContext.getMessageHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            Map<String, Object> msg = history.get(i);
            if ("user".equals(msg.get("role"))) {
                return (String) msg.get("content");
            }
        }

        return "";
    }

    /**
     * 判断事件文本是否是工具执行结果的重复
     * <p>SpringAI 自动执行工具后，模型会在文本中描述工具结果，
     * 但我们已经在 stateDelta 中获取了原始结果，避免重复展示
     */
    private boolean isToolResultText(String text, Event event) {
        // 检查是否是 FunctionResponse 事件（工具响应）
        if (event.content().isPresent()) {
            Content content = event.content().get();
            // 检查是否包含 FunctionResponse parts
            if (content.parts().isPresent()) {
                for (com.google.genai.types.Part part : content.parts().get()) {
                    if (part.functionResponse().isPresent()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 从 stateDelta key 解析工具名称
     */
    private String resolveToolName(String stateKey) {
        // 已知映射
        String mapped = STATE_DELTA_TOOL_MAPPING.get(stateKey);
        if (mapped != null) {
            return mapped;
        }

        // 从 key 推断：去掉 _result 后缀
        if (stateKey.endsWith("_result")) {
            return stateKey.substring(0, stateKey.length() - 7);
        }

        return stateKey;
    }

    /**
     * 格式化 stateDelta 值为字符串
     */
    private String formatStateValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Phase 1: 动态上下文注入
    // ═══════════════════════════════════════════════════════════════

    /**
     * 从工具结果中提取命令并记录到最近命令列表
     */
    private void recordExecutedCommand(DefaultReActFactory.DynamicContext dynamicContext, String toolResult) {
        if (toolResult.length() > 1000) {
            dynamicContext.addRecentCommand(truncate(toolResult, 80) + "...");
        } else {
            dynamicContext.addRecentCommand(toolResult);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }

    /**
     * 构建注入了动态上下文的用户消息
     * 委托 IPromptService 完成环境采集、里程碑获取、前缀构建
     */
    private String buildEnrichedMessage(String userMessage, DefaultReActFactory.DynamicContext dynamicContext) {
        // 记录用户消息的里程碑
        promptService.detectAndRecordMilestone(dynamicContext.getSessionId(), "user", userMessage);

        // 委托领域服务构建富化消息
        return promptService.buildEnrichedMessage(
                userMessage,
                dynamicContext.getSessionId(),
                dynamicContext.getTerminalSessionId(),
                dynamicContext.getRecentCommands(),
                dynamicContext.getMessageHistory()
        );
    }

}
