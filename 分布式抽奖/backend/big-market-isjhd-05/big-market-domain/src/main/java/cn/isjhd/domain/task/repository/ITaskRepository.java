package cn.isjhd.domain.task.repository;

import cn.isjhd.domain.task.model.entity.TaskEntity;

import java.util.List;

/**
 * @Author: isjhd
 * @Date: 2025-12-09 16:12
 * @Description: 任务服务仓储接口
 */
public interface ITaskRepository {

    List<TaskEntity> queryNoSendMessageTaskList();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);

}
