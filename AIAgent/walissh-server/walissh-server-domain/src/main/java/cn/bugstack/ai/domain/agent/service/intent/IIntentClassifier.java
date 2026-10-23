package cn.bugstack.ai.domain.agent.service.intent;

import cn.bugstack.ai.domain.agent.model.valobj.intent.ConversationContextVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;

public interface IIntentClassifier {
    IntentResultVO classify(String message, ConversationContextVO context);
}
