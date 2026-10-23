package cn.bugstack.ai.domain.agent.service.intent;

import cn.bugstack.ai.domain.agent.model.valobj.intent.ConversationContextVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentTypeEnumVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;

// 接收用户输入，调用兼容 OpenAI 协议的大模型，返回标准化的运维意图分类结果
@Component
public class LLMIntentClassifier implements IIntentClassifier {

    // Spring AI 聊天模型核心接口
    private ChatModel chatModel;

    @Value("${intent-ai-api.base-url}")
    private String apiHost;

    @Value("${intent-ai-api.api-key}")
    private String apiKey;

    @Value("${intent-ai-api.completions-path}")
    private String completionsPath;

    @Value("${intent-ai-api.chat-model.model}")
    private String model;


    @PostConstruct// Spring  bean 初始化完成后执行
    public void init() {
        // 构建 OpenAI 协议的 API 客户端
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(apiHost)
                .apiKey(apiKey)
                .completionsPath(completionsPath)
                .build();
        // 构建 Spring AI 聊天模型，设置低温度（保证结果确定性）
        this.chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(0.1) // 意图识别需要更确定的结果
                        .build())
                .build();
    }

    private static final String CLASSIFY_PROMPT_TEMPLATE = """
        你是一个 SSH 运维场景的意图识别系统。分析用户输入，返回 JSON 格式的意图分类结果。
        
        ## 意图类型
        - DIAGNOSE: 诊断问题（服务挂了、报错、异常排查）
        - CONFIGURE: 配置修改（改配置文件、调参数）
        - DEPLOY: 部署操作（部署、发布、回滚）
        - MONITOR: 监控查看（看日志、查状态、看资源使用）
        - SECURITY: 安全相关（防火墙、权限、证书）
        - BACKUP: 备份恢复（备份数据、恢复数据）
        - EXECUTE: 直接执行（帮我跑某命令）
        - EXPLAIN: 解释说明（这个命令什么意思）
        - SEARCH: 搜索查找（找文件、查进程）
        - CHAT: 闲聊
        - CONTINUE: 继续上一个任务
        - UNKNOWN: 无法判断
        
        ## 输出格式（仅返回 JSON，无其他内容）
        {"intent":"类型","confidence":0.0-1.0,"entities":{"key":"value"}}
        
        ## 示例
        输入: "nginx 502了，帮我看看"
        输出: {"intent":"DIAGNOSE","confidence":0.95,"entities":{"service":"nginx","error":"502"}}
        
        输入: "帮我改下 redis 的 maxmemory 配置"
        输出: {"intent":"CONFIGURE","confidence":0.9,"entities":{"service":"redis","config":"maxmemory"}}
        
        输入: "看下服务器磁盘使用情况"
        输出: {"intent":"MONITOR","confidence":0.9,"entities":{"resource":"disk"}}
        
        输入: "这个命令 awk '{print $1}' access.log 是什么意思"
        输出: {"intent":"EXPLAIN","confidence":0.95,"entities":{"command":"awk"}}
        
        ## 对话上下文
        最近意图: %s
        
        ## 分析以下输入
        输入: "%s"
        输出:
        """;

    @Override
    public IntentResultVO classify(String message, ConversationContextVO context) {
        // 拼接对话上下文（最近意图）
        String recentIntents = "";
        if (context != null && context.getRecentIntents() != null) {
            recentIntents = context.getRecentIntents().stream()
                .map(h -> h.getIntent().name())
                .collect(Collectors.joining(", "));
        }

        // 格式化 Prompt
        String prompt = String.format(CLASSIFY_PROMPT_TEMPLATE,
            recentIntents.isEmpty() ? "无" : recentIntents, message);

        // 调用大模型 + 解析响应 + 异常降级
        try {
            String response = chatModel.call(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            return IntentResultVO.builder()
                .intent(IntentTypeEnumVO.UNKNOWN).confidence(0.0).entities(Map.of()).build();
        }
    }

    @SuppressWarnings("unchecked")
    private IntentResultVO parseResponse(String response) {
        try {
            // 正则提取大模型返回的 JSON 字符串
            String json = response.replaceAll("(?s).*?(\\{.*}).*", "$1");
            // 反序列化为 Map，再封装为 VO
            Map<String, Object> parsed = new ObjectMapper().readValue(json, Map.class);
            // 枚举转换、参数校验
            IntentTypeEnumVO intent = IntentTypeEnumVO.valueOf(
                String.valueOf(parsed.get("intent")).toUpperCase());
            double confidence = parsed.containsKey("confidence")
                ? Double.parseDouble(String.valueOf(parsed.get("confidence"))) : 0.5;
            Map<String, String> entities = parsed.containsKey("entities")
                ? (Map<String, String>) parsed.get("entities") : Map.of();

            return IntentResultVO.builder()
                .intent(intent).confidence(confidence)
                .entities(entities).rawResponse(response).build();
        } catch (Exception e) {
            return IntentResultVO.builder()
                .intent(IntentTypeEnumVO.UNKNOWN).confidence(0.0)
                .entities(Map.of()).rawResponse(response).build();
        }
    }
}
