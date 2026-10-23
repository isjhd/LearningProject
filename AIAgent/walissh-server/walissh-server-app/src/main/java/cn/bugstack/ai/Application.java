package cn.bugstack.ai;

import cn.bugstack.ai.domain.agent.service.armory.matter.mcp.server.MyTestMcpService;
import cn.bugstack.ai.domain.agent.service.armory.matter.mcp.server.SshExecuteMcpService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Configurable
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    /** 测试工具：大小写转换 */
    @Bean("myToolCallbackProvider")
    public ToolCallbackProvider testTools(MyTestMcpService toolService) {
        return MethodToolCallbackProvider.builder().toolObjects(toolService).build();
    }

    /** SSH 命令执行工具 */
    @Bean("sshToolCallbackProvider")
    public ToolCallbackProvider sshTools(SshExecuteMcpService sshService) {
        return MethodToolCallbackProvider.builder().toolObjects(sshService).build();
    }

}
