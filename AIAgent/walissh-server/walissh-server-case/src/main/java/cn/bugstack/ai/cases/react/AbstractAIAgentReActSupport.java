package cn.bugstack.ai.cases.react;

import cn.bugstack.ai.api.dto.ChatRequestDTO;
import cn.bugstack.ai.api.dto.ReActEventDTO;
import cn.bugstack.ai.api.dto.ReActResultDTO;
import cn.bugstack.ai.cases.react.factory.DefaultReActFactory;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * ReAct 支撑类（抽象基类）
 *
 * <p>参考 mobile-claw-case 的 AbstractAutoAgentSupport 设计，
 * 封装 ReAct 循环的通用能力：
 * - 上下文管理（DynamicContext）
 * - SSE 事件发射
 * - 工具调用结果解析
 * - 响应格式化
 *
 * <p>节点路由链：
 * RootNode → AiCallNode → ToolCallNode → (ToolResultNode) → [循环或完成]
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/5/4 13:58
 */
@Slf4j
public abstract class AbstractAIAgentReActSupport extends AbstractMultiThreadStrategyRouter<ChatRequestDTO, DefaultReActFactory.DynamicContext, ReActResultDTO> {

    @Getter
    @Setter
    protected StrategyHandler<ChatRequestDTO, DefaultReActFactory.DynamicContext, ReActResultDTO> defaultStrategyHandler = StrategyHandler.DEFAULT;

    @Resource
    protected ApplicationContext applicationContext;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 会话 → 终端会话 ID 映射
     */
    protected static final Map<String, String> sessionTerminalMapping = new ConcurrentHashMap<>();

    /**
     * 当前线程绑定的终端会话 ID
     */
    protected static final InheritableThreadLocal<String> currentTerminalSession = new InheritableThreadLocal<>();

    @Override
    protected void multiThread(ChatRequestDTO requestParameter, DefaultReActFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 暂无异步预加载需求
    }

    /**
     * 通用的 Bean 获取
     */
    protected <T> T getBean(String beanName) {
        return applicationContext.getBean(beanName, (Class<T>) Object.class);
    }

    // ═══════════════════════════════════════════════════════════════
    //  上下文绑定（ThreadLocal 方式，兼容异步线程）
    // ═══════════════════════════════════════════════════════════════

    /**
     * 设置当前线程的终端会话 ID
     */
    protected static void setCurrentTerminalSession(String terminalSessionId) {
        currentTerminalSession.set(terminalSessionId);
    }

    /**
     * 获取当前线程的终端会话 ID
     */
    protected static String getCurrentTerminalSession() {
        return currentTerminalSession.get();
    }

    /**
     * 清除当前线程的终端会话 ID
     */
    protected static void clearCurrentTerminalSession() {
        currentTerminalSession.remove();
    }

    /**
     * 绑定会话与终端会话
     */
    protected static void bindTerminalSession(String sessionId, String terminalSessionId) {
        if (sessionId != null && terminalSessionId != null) {
            sessionTerminalMapping.put(sessionId, terminalSessionId);
        }
    }

    /**
     * 获取会话绑定的终端会话 ID
     */
    protected static String getTerminalSession(String sessionId) {
        return sessionId != null ? sessionTerminalMapping.get(sessionId) : null;
    }

    /**
     * 解绑会话与终端会话
     */
    protected static void unbindTerminalSession(String sessionId) {
        if (sessionId != null) {
            sessionTerminalMapping.remove(sessionId);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SSE 事件发射辅助
    // ═══════════════════════════════════════════════════════════════

    /**
     * 发送文本事件
     */
    protected void sendTextEvent(ResponseBodyEmitter emitter, String content, String fullText) {
        try {
            ReActEventDTO event = new ReActEventDTO();
            event.setEvent("text");
            event.setContent(content);
            event.setFullText(fullText);
            emitter.send(objectMapper.writeValueAsString(event) + "\n");
            log.info("发送文本事件 {}", event);
        } catch (Exception e) {
            log.warn("发送文本事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送工具调用事件
     */
    protected void sendToolCallEvent(ResponseBodyEmitter emitter, String toolCallId, String toolName, String status) {
        try {
            ReActEventDTO event = new ReActEventDTO();
            event.setEvent("tool_call");
            event.setToolCallId(toolCallId);
            event.setToolName(toolName);
            event.setStatus(status);
            emitter.send(objectMapper.writeValueAsString(event) + "\n");
            log.info("发送工具调用事件 {}", event);
        } catch (Exception e) {
            log.warn("发送工具调用事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送工具结果事件
     */
    protected void sendToolResultEvent(ResponseBodyEmitter emitter, String toolCallId, String content, String status) {
        try {
            ReActEventDTO event = new ReActEventDTO();
            event.setEvent("tool_result");
            event.setToolCallId(toolCallId);
            event.setContent(content);
            event.setStatus(status);
            emitter.send(objectMapper.writeValueAsString(event) + "\n");
            log.info("发送工具结果事件 {}", event);
        } catch (Exception e) {
            log.warn("发送工具结果事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送步数结束事件
     */
    protected void sendRoundEndEvent(ResponseBodyEmitter emitter, int currentStep, int maxSteps, boolean shouldContinue, int totalToolCalls) {
        try {
            ReActEventDTO.StepInfo stepInfo = new ReActEventDTO.StepInfo();
            stepInfo.setCurrentStep(currentStep);
            stepInfo.setMaxSteps(maxSteps);
            stepInfo.setShouldContinue(shouldContinue);
            stepInfo.setTotalToolCalls(totalToolCalls);

            ReActEventDTO event = new ReActEventDTO();
            event.setEvent("round_end");
            event.setStepInfo(stepInfo);
            emitter.send(objectMapper.writeValueAsString(event) + "\n");
            log.info("发送 round_end 事件 {}", event);
        } catch (Exception e) {
            log.warn("发送 round_end 事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送完成事件
     */
    protected void sendDoneEvent(ResponseBodyEmitter emitter, ReActResultDTO result) {
        try {
            ReActEventDTO event = new ReActEventDTO();
            event.setEvent("done");
            event.setContent(objectMapper.writeValueAsString(result));
            emitter.send(objectMapper.writeValueAsString(event) + "\n");
            log.info("发送 done 事件 {}", event);
        } catch (Exception e) {
            log.warn("发送 done 事件失败: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  工具调用结果解析（参考 mobile-claw-case）
    // ═══════════════════════════════════════════════════════════════

    /**
     * 解析 AI 响应中的 action 字符串
     * 兼容格式：
     * 1. JSON: ```json { "action": "..." } ```
     * 2. <answer>...</answer> 标签包裹的内容
     * 3. DSL: do(action=...) / finish(message=...)
     */
    protected String parseActionString(String response) {
        if (response == null || response.isBlank()) return null;

        String contentToParse = response;

        // 1. 提取 <answer>...</answer> 标签
        java.util.regex.Pattern answerPattern = java.util.regex.Pattern.compile("<answer>(.*?)</answer>", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher answerMatcher = answerPattern.matcher(response);
        if (answerMatcher.find()) {
            contentToParse = answerMatcher.group(1).trim();
        }

        // 2. 尝试 JSON
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(contentToParse);
            if (jsonNode.has("action")) {
                return jsonNode.get("action").asText();
            }
        } catch (Exception ignored) {
        }

        // 3. 尝试 markdown JSON 代码块
        java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("```json(.*?)```", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(contentToParse);
        if (jsonMatcher.find()) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(jsonMatcher.group(1).trim());
                if (jsonNode.has("action")) {
                    return jsonNode.get("action").asText();
                }
            } catch (Exception ignored) {
            }
        }

        // 4. 尝试 DSL
        java.util.regex.Pattern dslPattern = java.util.regex.Pattern.compile("(do|finish)\\s*\\(\\s*(action|message)\\s*=", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher dslMatcher = dslPattern.matcher(contentToParse);
        int lastStart = -1;
        while (dslMatcher.find()) {
            lastStart = dslMatcher.start();
        }
        if (lastStart != -1) {
            String action = contentToParse.substring(lastStart).trim();
            if (action.endsWith("```")) {
                action = action.substring(0, action.length() - 3).trim();
            }
            return action;
        }

        return null;
    }

    /**
     * 判断响应是否包含工具调用（用于决定是否继续循环）
     * 兼容两种格式：
     * - WaLiCode 风格：直接解析 tool_calls JSON
     * - mobile-claw-case 风格：解析 action 字符串
     */
    protected boolean hasToolCalls(String response) {
        if (response == null || response.isBlank()) return false;

        // 1. 检查 <answer> 标签内容
        java.util.regex.Pattern answerPattern = java.util.regex.Pattern.compile("<answer>(.*?)</answer>", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher answerMatcher = answerPattern.matcher(response);
        if (answerMatcher.find()) {
            String content = answerMatcher.group(1).trim();
            return containsToolCallSign(content);
        }

        return containsToolCallSign(response);
    }

    private boolean containsToolCallSign(String content) {
        // 检查 do(...) / finish(...) DSL 模式
        if (java.util.regex.Pattern.compile("(do|finish)\\s*\\(").matcher(content).find()) {
            return true;
        }
        // 检查 tool_calls JSON 结构
        if (content.contains("tool_calls") || content.contains("\"action\"")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否应该终止循环
     * 终止条件：检测到 finish / max_steps / user_stop / error
     */
    protected boolean shouldTerminate(String response, int currentStep, int maxSteps) {
        if (response == null || response.isBlank()) return false;

        String actionStr = parseActionString(response);

        // finish → 终止
        if (actionStr != null && actionStr.startsWith("finish")) {
            return true;
        }

        // 达到最大步数 → 终止
        if (currentStep >= maxSteps) {
            return true;
        }

        // 检测错误关键词
        String lower = response.toLowerCase();
        if (lower.contains("error:") || lower.contains("failed:")) {
            return true;
        }

        return false;
    }

}
