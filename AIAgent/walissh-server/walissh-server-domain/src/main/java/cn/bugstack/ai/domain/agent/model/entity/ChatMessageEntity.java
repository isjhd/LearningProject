package cn.bugstack.ai.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 对话消息实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEntity {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private String toolName;
    private String toolCallId;
    private String priority;
    private Integer tokenCount;
    private Date createdAt;
}
