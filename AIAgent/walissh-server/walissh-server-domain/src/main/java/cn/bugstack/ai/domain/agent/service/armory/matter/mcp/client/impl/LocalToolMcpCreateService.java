package cn.bugstack.ai.domain.agent.service.armory.matter.mcp.client.impl;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * 本地 Spring AI ToolCallbackProvider 工具构建服务
 *
 * 用于处理通过 Bean 注册的 ToolCallbackProvider（如 sshToolCallbackProvider），
 * 区别于 SSE/Stdio MCP 服务。
 */
@Slf4j
@Service
public class LocalToolMcpCreateService implements TooMcpCreateService {

    // Spring 应用上下文（构造器注入，Spring官方推荐）
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception {
        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.LocalParameters local = toolMcp.getLocal();
        if (local == null) {
            log.warn("LocalParameters 为空");
            return new ToolCallback[0];
        }

        // 2. 获取配置的Bean名称
        String name = local.getName();
        log.info("从 Spring 上下文加载 ToolCallbackProvider: {}", name);

        // 从配置获取 bean 名称
        String providerBeanName = name;

        // 尝试从 Spring 上下文获取 ToolCallbackProvider bean
        ToolCallbackProvider provider = null;
        try {
            // 3. 从Spring容器获取Bean
            provider = applicationContext.getBean(providerBeanName, ToolCallbackProvider.class);
        } catch (Exception e) {
            log.warn("未找到 ToolCallbackProvider bean: {}", providerBeanName);
        }

        if (provider == null) {
            // 尝试不使用类型限定查找
            try {
                Object bean = applicationContext.getBean(providerBeanName);
                if (bean instanceof ToolCallbackProvider) {
                    provider = (ToolCallbackProvider) bean;
                }
            } catch (Exception e) {
                log.warn("ToolCallbackProvider bean 类型不匹配: {}", providerBeanName);
            }
        }

        if (provider == null) {
            log.error("未找到 ToolCallbackProvider: {}", providerBeanName);
            return new ToolCallback[0];
        }

        // 4. 获取并返回工具回调数组
        ToolCallback[] callbacks = provider.getToolCallbacks();
        log.info("ToolCallbackProvider '{}' 返回 {} 个工具", providerBeanName, callbacks.length);

        return callbacks;
    }
}
