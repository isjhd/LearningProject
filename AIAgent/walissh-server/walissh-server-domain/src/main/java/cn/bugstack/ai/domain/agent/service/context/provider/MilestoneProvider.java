package cn.bugstack.ai.domain.agent.service.context.provider;

import cn.bugstack.ai.domain.agent.model.valobj.prompt.MilestoneVO;
import cn.bugstack.ai.domain.agent.service.prompt.dynamic.MilestoneTracker;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 专门从底层获取最近 10 条任务里程碑；
// 最终让 AI 掌握任务进度，实现更智能的任务型对话。
// 任务进度层：任务做到哪一步了？
@Component
public class MilestoneProvider implements ContextProvider {


    @Resource
    private MilestoneTracker milestoneTracker;

    @Override public String getName() { return "milestoneVO"; }
    @Override public int getOrder() { return 30; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory) {
        Map<String, Object> result = new HashMap<>();
        // 核心：获取当前会话最近10条任务里程碑
        List<MilestoneVO> milestoneVOS = milestoneTracker.getRecent(sessionId, 10);
        // 封装为固定key的Map，交给ChatContextService合并
        result.put("milestoneVOS", milestoneVOS);
        return result;
    }
}
