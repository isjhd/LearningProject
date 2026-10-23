package cn.bugstack.ai.domain.ssh.adapter.repository;

import cn.bugstack.ai.domain.ssh.model.entity.SshConnectionEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshConnectionConfigEntity;

import java.util.List;

/**
 * SSH连接仓储接口（领域层定义，基础设施层实现）
 *
 * @author waissh dev
 */
public interface ISshConnectionRepository {

    /**
     * 保存SSH连接配置
     */
    void saveConnection(SshConnectionEntity entity);

    /**
     * 更新SSH连接配置
     */
    void updateConnection(SshConnectionEntity entity);

    /**
     * 删除SSH连接配置
     */
    void deleteConnection(String connectionId);

    /**
     * 根据连接ID查询
     */
    SshConnectionEntity queryConnectionById(String connectionId);

    /**
     * 查询用户的所有连接
     */
    List<SshConnectionEntity> queryConnectionListByUserId(String userId);

    /**
     * 保存/更新高级配置
     */
    void saveConnectionConfig(SshConnectionConfigEntity entity);

    /**
     * 根据连接ID查询高级配置
     */
    SshConnectionConfigEntity queryConnectionConfigById(String connectionId);

}
