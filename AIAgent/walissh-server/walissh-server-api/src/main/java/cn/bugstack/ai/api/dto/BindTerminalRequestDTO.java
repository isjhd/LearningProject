package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绑定终端请求 DTO
 *
 * @author waissh dev
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindTerminalRequestDTO {
    /**
     * 智能体会话 ID
     */
    private String chatSessionId;

    /**
     * SSH 终端会话 ID
     */
    private String terminalSessionId;
}
