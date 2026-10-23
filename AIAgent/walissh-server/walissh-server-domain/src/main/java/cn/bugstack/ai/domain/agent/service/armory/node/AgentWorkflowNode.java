package cn.bugstack.ai.domain.agent.service.armory.node;

import cn.bugstack.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.bugstack.ai.domain.agent.model.valobj.enums.AgentTypeEnum;
import cn.bugstack.ai.domain.agent.service.armory.AbstractArmorySupport;
import cn.bugstack.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.ai.domain.agent.service.armory.node.workflow.LoopAgentNode;
import cn.bugstack.ai.domain.agent.service.armory.node.workflow.ParallelAgentNode;
import cn.bugstack.ai.domain.agent.service.armory.node.workflow.SequentialAgentNode;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class AgentWorkflowNode extends AbstractArmorySupport {

    @Resource
    private LoopAgentNode loopAgentNode;// 循环节点
    @Resource
    private ParallelAgentNode parallelAgentNode;// 并行节点
    @Resource
    private SequentialAgentNode sequentialAgentNode;// 串行节点
    @Resource
    private RunnerNode runnerNode;// 默认执行节点


    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - AgentWorkflowNode");

        // 1. 获取配置的工作流列表
        AiAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = aiAgentConfigTableVO.getModule().getAgentWorkflows();

        // 2. 边界判断：无工作流 / 已执行完所有步骤
        if (null == agentWorkflows || agentWorkflows.isEmpty() || dynamicContext.getCurrentStepIndex() >= agentWorkflows.size()) {
            // 清空当前工作流
            dynamicContext.setCurrentAgentWorkflow(null);
            // 路由到下一个节点
            return router(requestParameter, dynamicContext);
        }

        // 3. 设置当前要执行的工作流（根据步骤索引）
        dynamicContext.setCurrentAgentWorkflow(agentWorkflows.get(dynamicContext.getCurrentStepIndex()));

        // 4. 步骤索引+1，为下一次执行做准备
        dynamicContext.addCurrentStepIndex();

        // 5. 路由到get()方法匹配的处理器节点
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {

        // 1. 从上下文获取当前工作流
        AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();

        // 2. 无工作流 → 返回默认处理器
        if (null == currentAgentWorkflow){
            return runnerNode;
        }

        // 3. 根据类型字符串匹配枚举
        String type = currentAgentWorkflow.getType();
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.formType(type);

        if (null == agentTypeEnum){
            throw new RuntimeException("agentWorkflow type is error!");
        }

        String node = agentTypeEnum.getNode();

        // 4. SWITCH匹配对应的节点处理器
        return switch (node){
            case "loopAgentNode" -> loopAgentNode;
            case "parallelAgentNode" -> parallelAgentNode;
            case "sequentialAgentNode" -> sequentialAgentNode;
            default -> runnerNode;
        };
    }

}
