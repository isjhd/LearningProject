package cn.bugstack.ai.domain.agent.service;

import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;

// 意图识别服务
public interface IIntentService {
    IntentResultVO classify(String sessionId, String userId, String message);
}
