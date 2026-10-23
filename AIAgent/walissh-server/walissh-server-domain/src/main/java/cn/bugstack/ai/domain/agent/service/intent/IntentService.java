package cn.bugstack.ai.domain.agent.service.intent;

import cn.bugstack.ai.domain.agent.model.valobj.intent.ConversationContextVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;
import cn.bugstack.ai.domain.agent.service.IIntentService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// 编排层
@Service
public class IntentService implements IIntentService {

    // 注入两个分类器 + 上下文追踪器
    @Resource
    private RuleIntentClassifier ruleClassifier;
    @Resource
    private LLMIntentClassifier llmClassifier;
    @Resource
    private ContextTracker contextTracker;

    // 自定义 LRU 缓存：最大200条，自动淘汰最旧数据
    private final Map<String, CacheEntry> cache = Collections.synchronizedMap(
        new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > 200;
            }
        });

    // 缓存实体：存储分类结果 + 过期时间
    private static class CacheEntry {
        IntentResultVO result;
        long expireTime;
        CacheEntry(IntentResultVO result, long expireTime) {
            this.result = result;
            this.expireTime = expireTime;
        }
    }

    @Override
    public IntentResultVO classify(String sessionId, String userId, String message) {
        // 1. 生成缓存键：sessionId(会话隔离) + 消息哈希
        String cacheKey = sessionId + ":" + hashMessage(message);
        // 2. 缓存命中且未过期 → 直接返回
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && cached.expireTime > System.currentTimeMillis()) {
            return cached.result;
        }

        // 3. 获取对话上下文
        ConversationContextVO context = contextTracker.getContext(sessionId);

        // 4. 第一层：规则引擎（<1ms），置信度≥0.8 直接返回
        IntentResultVO ruleResult = ruleClassifier.classify(message, context);
        if (ruleResult.getConfidence() >= 0.8) {
            recordAndCache(sessionId, cacheKey, ruleResult);
            return ruleResult;
        }

        // 5. 第二层：LLM大模型（100-500ms），置信度≥0.5 用AI结果，否则兜底规则
        IntentResultVO llmResult = llmClassifier.classify(message, context);
        IntentResultVO finalResult = llmResult.getConfidence() >= 0.5 ? llmResult : ruleResult;

        // 6. 记录上下文 + 写入缓存
        recordAndCache(sessionId, cacheKey, finalResult);
        return finalResult;
    }

    // 记录上下文 + 写入缓存（5分钟过期）
    private void recordAndCache(String sessionId, String cacheKey, IntentResultVO result) {
        contextTracker.updateContext(sessionId, result);
        // 缓存 5 分钟

        cache.put(cacheKey, new CacheEntry(result, System.currentTimeMillis() + 5 * 60 * 1000));
    }

    private String hashMessage(String message) {
        return Integer.toHexString(message.hashCode());
    }
}
