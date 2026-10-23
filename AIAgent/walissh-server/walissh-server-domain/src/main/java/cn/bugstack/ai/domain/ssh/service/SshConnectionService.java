package cn.bugstack.ai.domain.ssh.service;

import cn.bugstack.ai.domain.ssh.adapter.repository.ISshConnectionRepository;
import cn.bugstack.ai.domain.ssh.adapter.port.ISshSessionPort;
import cn.bugstack.ai.domain.ssh.model.entity.SshConnectionEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshConnectionConfigEntity;
import cn.bugstack.ai.domain.ssh.model.valobj.ConnectionStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 封装 SSH 连接的业务规则、状态流转、数据持久化
 *
 * @author waissh dev
 */
@Slf4j
@Service
public class SshConnectionService implements ISshConnectionDomainService {

    // SSH 连接仓储接口（数据持久化层，操作数据库）
    private final ISshConnectionRepository repository;
    // SSH 会话端口接口（基础设施层，封装底层 SSH 连接操作）
    private final ISshSessionPort sshSessionService;

    public SshConnectionService(ISshConnectionRepository repository, ISshSessionPort sshSessionService) {
        this.repository = repository;
        this.sshSessionService = sshSessionService;
    }

    @Override
    public void createConnection(SshConnectionEntity entity, SshConnectionConfigEntity configEntity) {
        // 1. 校验必填字段
        entity.validate();

        // 2. 生成连接ID
        if (entity.getConnectionId() == null || entity.getConnectionId().isBlank()) {
            entity.setConnectionId(UUID.randomUUID().toString().replace("-", ""));
        }

        // 3. 设置默认值
        entity.setStatus(ConnectionStatusEnum.DISCONNECTED);
        if (entity.getPort() == null) {
            entity.setPort(22);//设置端口号
        }
        if (entity.getEncrypted() == null) {
            entity.setEncrypted(1);//设置加密类型
        }
        if (entity.getUserId() == null || entity.getUserId().isBlank()) {
            entity.setUserId("default");
        }

        // 4. 保存连接
        repository.saveConnection(entity);

        // 5. 保存高级配置
        if (configEntity != null) {
            configEntity.setConnectionId(entity.getConnectionId());
            configEntity.withDefaults();
            repository.saveConnectionConfig(configEntity);
        }

        log.info("SSH连接创建成功 connectionId={}", entity.getConnectionId());
    }

    @Override
    public void updateConnection(SshConnectionEntity entity, SshConnectionConfigEntity configEntity) {
        // 1. 校验必填字段
        entity.validate();

        // 2. 检查连接是否存在，并获取原有数据
        SshConnectionEntity existing = repository.queryConnectionById(entity.getConnectionId());
        if (existing == null) {
            throw new IllegalArgumentException("连接不存在");
        }

        // 3. 密码/私钥留空则保留原值
        if (entity.getPassword() == null || entity.getPassword().isEmpty()) {
            entity.setPassword(existing.getPassword());
        }
        if (entity.getPrivateKey() == null || entity.getPrivateKey().isEmpty()) {
            entity.setPrivateKey(existing.getPrivateKey());
        }
        // encrypted 保留原值
        if (entity.getEncrypted() == null) {
            entity.setEncrypted(existing.getEncrypted());
        }

        // 4. 更新连接
        repository.updateConnection(entity);

        // 5. 更新高级配置
        if (configEntity != null) {
            configEntity.setConnectionId(entity.getConnectionId());
            repository.saveConnectionConfig(configEntity);
        }

        log.info("SSH连接更新成功 connectionId={}", entity.getConnectionId());
    }

    @Override
    public void deleteConnection(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            throw new IllegalArgumentException("连接ID不能为空");
        }
        repository.deleteConnection(connectionId);
        log.info("SSH连接删除成功 connectionId={}", connectionId);
    }

    @Override
    public SshConnectionEntity getConnection(String connectionId) {
        return repository.queryConnectionById(connectionId);
    }

    @Override
    public List<SshConnectionEntity> getConnectionList(String userId) {
        if (userId == null || userId.isBlank()) {
            userId = "default";
        }
        return repository.queryConnectionListByUserId(userId);
    }

    @Override
    public SshConnectionConfigEntity getConnectionConfig(String connectionId) {
        return repository.queryConnectionConfigById(connectionId);
    }

    @Override
    public boolean connect(String connectionId) {
        // 1. 查询连接信息
        SshConnectionEntity entity = repository.queryConnectionById(connectionId);
        if (entity == null) {
            throw new IllegalArgumentException("连接不存在");
        }

        // 2. 建立 SSH 连接
        boolean success = sshSessionService.connect(
                connectionId,
                entity.getHost(),
                entity.getPort(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getPrivateKey()
        );

        // 3. 更新连接状态
        entity.setStatus(success ? ConnectionStatusEnum.CONNECTED : ConnectionStatusEnum.FAILED);
        repository.updateConnection(entity);

        return success;
    }

    @Override
    public void disconnect(String connectionId) {
        // 1. 断开 SSH 连接
        sshSessionService.disconnect(connectionId);

        // 2. 更新连接状态
        SshConnectionEntity entity = repository.queryConnectionById(connectionId);
        if (entity != null) {
            entity.setStatus(ConnectionStatusEnum.DISCONNECTED);
            repository.updateConnection(entity);
        }
    }

    @Override
    public boolean isConnected(String connectionId) {
        return sshSessionService.isConnected(connectionId);
    }

}
