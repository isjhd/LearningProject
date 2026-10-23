package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绑定终端响应 DTO
 *
 * @author waissh dev
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindTerminalResponseDTO {
    /**
     * 智能体会话 ID
     */
    private String chatSessionId;

    /**
     * SSH 终端会话 ID
     */
    private String terminalSessionId;

    /**
     * 是否已绑定
     */
    private Boolean bound;
}
