package cn.isjhd.domain.task.model.entity;

import lombok.Data;

/**
 * @Author: isjhd
 * @Date: 2025-12-09 16:02
 * @Description: 任务实体对象
 */
@Data
public class TaskEntity {

    /** 活动ID */
    private String userId;
    /** 消息主题 */
    private String topic;
    /** 消息编号 */
    private String messageId;
    /** 消息主体 */
    private String message;

}
