package cn.bugstack.ai.domain.agent.model.valobj.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationContextVO {
    private LinkedList<IntentHistoryEntryVO> recentIntents;
    private int turnCount;
    private long sessionStartTime;
    private IntentTypeEnumVO lastIntent;
}
