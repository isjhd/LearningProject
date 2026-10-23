package cn.bugstack.ai.domain.agent.model.valobj.prompt;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PromptContextVO {

    private String serverInfo;
    private String osInfo;
    private String currentUser;
    private String currentDirectory;

    private List<String> recentCommands;

    private List<MilestoneVO> milestoneVOS;
    
    private String toolResultSummary;
    
    private String taskDescription;
}
