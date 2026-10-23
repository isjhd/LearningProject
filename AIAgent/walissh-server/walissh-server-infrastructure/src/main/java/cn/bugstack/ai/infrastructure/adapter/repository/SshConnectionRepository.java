package cn.bugstack.ai.infrastructure.adapter.repository;

import cn.bugstack.ai.domain.ssh.adapter.repository.ISshConnectionRepository;
import cn.bugstack.ai.domain.ssh.model.entity.SshConnectionConfigEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshConnectionEntity;
import cn.bugstack.ai.domain.ssh.model.valobj.AuthTypeEnum;
import cn.bugstack.ai.domain.ssh.model.valobj.ConnectionStatusEnum;
import cn.bugstack.ai.infrastructure.dao.ISshConnectionConfigDAO;
import cn.bugstack.ai.infrastructure.dao.ISshConnectionDAO;
import cn.bugstack.ai.infrastructure.dao.po.SshConnectionConfigPO;
import cn.bugstack.ai.infrastructure.dao.po.SshConnectionPO;
import cn.bugstack.ai.infrastructure.security.PasswordEncryptor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SSH连接仓储实现
 *
 * @author waissh dev
 */
@Repository
public class SshConnectionRepository implements ISshConnectionRepository {

    @Resource
    private ISshConnectionDAO sshConnectionDAO;
    @Resource
    private ISshConnectionConfigDAO sshConnectionConfigDAO;

    private final PasswordEncryptor passwordEncryptor = new PasswordEncryptor();

    @Override
    public void saveConnection(SshConnectionEntity entity) {
        sshConnectionDAO.insert(toPO(entity));
    }

    @Override
    public void updateConnection(SshConnectionEntity entity) {
        sshConnectionDAO.update(toPO(entity));
    }

    @Override
    public void deleteConnection(String connectionId) {
        sshConnectionDAO.delete(connectionId);
    }

    @Override
    public SshConnectionEntity queryConnectionById(String connectionId) {
        SshConnectionPO po = sshConnectionDAO.queryByConnectionId(connectionId);
        return po != null ? toEntity(po) : null;
    }

    @Override
    public List<SshConnectionEntity> queryConnectionListByUserId(String userId) {
        return sshConnectionDAO.queryListByUserId(userId).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveConnectionConfig(SshConnectionConfigEntity entity) {
        sshConnectionConfigDAO.insertOrUpdate(toConfigPO(entity));
    }

    @Override
    public SshConnectionConfigEntity queryConnectionConfigById(String connectionId) {
        SshConnectionConfigPO po = sshConnectionConfigDAO.queryByConnectionId(connectionId);
        return po != null ? toConfigEntity(po) : null;
    }

    // ========== Entity <-> PO 转换 ==========

    private SshConnectionPO toPO(SshConnectionEntity entity) {
        // 加密密码和私钥
        String encryptedPassword = entity.getPassword();
        String encryptedPrivateKey = entity.getPrivateKey();
        Integer encryptedFlag = entity.getEncrypted();

        if (encryptedPassword != null && !encryptedPassword.isEmpty() && !passwordEncryptor.isEncrypted(encryptedPassword)) {
            encryptedPassword = passwordEncryptor.encrypt(encryptedPassword);
            encryptedFlag = 1;
        }
        if (encryptedPrivateKey != null && !encryptedPrivateKey.isEmpty() && !passwordEncryptor.isEncrypted(encryptedPrivateKey)) {
            encryptedPrivateKey = passwordEncryptor.encrypt(encryptedPrivateKey);
            encryptedFlag = 1;
        }

        return SshConnectionPO.builder()
                .id(entity.getId())
                .connectionId(entity.getConnectionId())
                .connectionName(entity.getConnectionName())
                .host(entity.getHost())
                .port(entity.getPort())
                .username(entity.getUsername())
                .authType(entity.getAuthType() != null ? entity.getAuthType().getCode() : AuthTypeEnum.PASSWORD.getCode())
                .password(encryptedPassword)
                .privateKey(encryptedPrivateKey)
                .encrypted(encryptedFlag)
                .status(entity.getStatus() != null ? entity.getStatus().getCode() : ConnectionStatusEnum.DISCONNECTED.getCode())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SshConnectionEntity toEntity(SshConnectionPO po) {
        // 解密密码和私钥
        String password = po.getPassword();
        String privateKey = po.getPrivateKey();

        if (po.getEncrypted() != null && po.getEncrypted() == 1) {
            if (password != null && !password.isEmpty()) {
                password = passwordEncryptor.decrypt(password);
            }
            if (privateKey != null && !privateKey.isEmpty()) {
                privateKey = passwordEncryptor.decrypt(privateKey);
            }
        }

        return SshConnectionEntity.builder()
                .id(po.getId())
                .connectionId(po.getConnectionId())
                .connectionName(po.getConnectionName())
                .host(po.getHost())
                .port(po.getPort())
                .username(po.getUsername())
                .authType(AuthTypeEnum.fromCode(po.getAuthType()))
                .password(password)
                .privateKey(privateKey)
                .encrypted(po.getEncrypted())
                .status(ConnectionStatusEnum.fromCode(po.getStatus()))
                .userId(po.getUserId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private SshConnectionConfigPO toConfigPO(SshConnectionConfigEntity entity) {
        return SshConnectionConfigPO.builder()
                .id(entity.getId())
                .connectionId(entity.getConnectionId())
                .connectTimeout(entity.getConnectTimeout())
                .keepaliveInterval(entity.getKeepaliveInterval())
                .startupCommand(entity.getStartupCommand())
                .compression(booleanToInt(entity.getCompression()))
                .strictHostKeyCheck(booleanToInt(entity.getStrictHostKeyCheck()))
                .knownHosts(entity.getKnownHosts())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SshConnectionConfigEntity toConfigEntity(SshConnectionConfigPO po) {
        return SshConnectionConfigEntity.builder()
                .id(po.getId())
                .connectionId(po.getConnectionId())
                .connectTimeout(po.getConnectTimeout())
                .keepaliveInterval(po.getKeepaliveInterval())
                .startupCommand(po.getStartupCommand())
                .compression(intToBoolean(po.getCompression()))
                .strictHostKeyCheck(intToBoolean(po.getStrictHostKeyCheck()))
                .knownHosts(po.getKnownHosts())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private Integer booleanToInt(Boolean val) {
        return Boolean.TRUE.equals(val) ? 1 : 0;
    }

    private Boolean intToBoolean(Integer val) {
        return val != null && val == 1;
    }

}
