package cn.bugstack.ai.trigger.http;

import cn.bugstack.ai.api.dto.*;
import cn.bugstack.ai.api.response.Response;
import cn.bugstack.ai.domain.agent.service.IChatService;
import cn.bugstack.ai.domain.agent.service.armory.matter.mcp.server.SshExecuteMcpService;
import cn.bugstack.ai.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH 智能体控制器
 * 提供智能体会话与 SSH 终端的绑定能力
 *
 * @author waissh dev
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ssh/agent")
@CrossOrigin(origins = "*")
public class SshAgentController {

    @Resource
    private IChatService chatService;

    // 会话绑定映射：chatSessionId → terminalSessionId
    private final Map<String, String> sessionBindings = new ConcurrentHashMap<>();

    /**
     * 绑定 SSH 终端到智能体会话
     *
     * @param requestDTO 绑定请求
     * @return 绑定结果
     */
    @RequestMapping(value = "bind_terminal", method = RequestMethod.POST)
    public Response<BindTerminalResponseDTO> bindTerminal(@RequestBody BindTerminalRequestDTO requestDTO) {
        try {
            String chatSessionId = requestDTO.getChatSessionId();
            String terminalSessionId = requestDTO.getTerminalSessionId();

            log.info("绑定终端会话: chatSessionId={}, terminalSessionId={}", chatSessionId, terminalSessionId);

            if (chatSessionId == null || chatSessionId.isEmpty()) {
                return Response.<BindTerminalResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("chatSessionId 不能为空")
                        .build();
            }

            if (terminalSessionId == null || terminalSessionId.isEmpty()) {
                return Response.<BindTerminalResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("terminalSessionId 不能为空")
                        .build();
            }

            // 存储绑定关系
            sessionBindings.put(chatSessionId, terminalSessionId);

            BindTerminalResponseDTO response = BindTerminalResponseDTO.builder()
                    .chatSessionId(chatSessionId)
                    .terminalSessionId(terminalSessionId)
                    .bound(true)
                    .build();

            return Response.<BindTerminalResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(response)
                    .build();

        } catch (Exception e) {
            log.error("绑定终端会话失败", e);
            return Response.<BindTerminalResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("绑定失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 解绑 SSH 终端
     *
     * @param chatSessionId 智能体会话 ID
     * @return 解绑结果
     */
    @RequestMapping(value = "unbind_terminal", method = RequestMethod.POST)
    public Response<Void> unbindTerminal(@RequestParam("chatSessionId") String chatSessionId) {
        try {
            log.info("解绑终端会话: chatSessionId={}", chatSessionId);

            sessionBindings.remove(chatSessionId);

            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();

        } catch (Exception e) {
            log.error("解绑终端会话失败", e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("解绑失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 查询会话绑定的终端
     *
     * @param chatSessionId 智能体会话 ID
     * @return 绑定信息
     */
    @RequestMapping(value = "query_binding", method = RequestMethod.GET)
    public Response<BindTerminalResponseDTO> queryBinding(@RequestParam("chatSessionId") String chatSessionId) {
        try {
            String terminalSessionId = sessionBindings.get(chatSessionId);

            if (terminalSessionId == null) {
                return Response.<BindTerminalResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("未绑定终端")
                        .data(BindTerminalResponseDTO.builder()
                                .chatSessionId(chatSessionId)
                                .bound(false)
                                .build())
                        .build();
            }

            return Response.<BindTerminalResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(BindTerminalResponseDTO.builder()
                            .chatSessionId(chatSessionId)
                            .terminalSessionId(terminalSessionId)
                            .bound(true)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("查询绑定失败", e);
            return Response.<BindTerminalResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取绑定的终端会话 ID
     * 供内部调用使用
     */
    public String getTerminalSessionId(String chatSessionId) {
        return sessionBindings.get(chatSessionId);
    }

    /**
     * 设置当前线程的终端会话（供工具调用前设置）
     */
    public void setCurrentTerminalSession(String chatSessionId) {
        String terminalSessionId = sessionBindings.get(chatSessionId);
        if (terminalSessionId != null) {
            SshExecuteMcpService.setCurrentTerminalSession(terminalSessionId);
        }
    }
}
