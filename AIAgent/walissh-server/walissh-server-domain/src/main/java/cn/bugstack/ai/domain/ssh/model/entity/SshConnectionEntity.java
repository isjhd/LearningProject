package cn.bugstack.ai.domain.ssh.model.entity;

import cn.bugstack.ai.domain.ssh.model.valobj.AuthTypeEnum;
import cn.bugstack.ai.domain.ssh.model.valobj.ConnectionStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSH连接配置实体
 *
 * @author waissh dev
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshConnectionEntity {

    private Long id;
    private String connectionId;
    private String connectionName;
    private String host;
    private Integer port;
    private String username;
    private AuthTypeEnum authType;
    private String password;
    private String privateKey;
    private Integer encrypted;
    private ConnectionStatusEnum status;
    private String userId;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    /**
     * 校验必填字段
     */
    public void validate() {
        if (connectionName == null || connectionName.isBlank()) {
            throw new IllegalArgumentException("连接名称不能为空");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("主机地址不能为空");
        }
        if (port == null || port <= 0 || port > 65535) {
            throw new IllegalArgumentException("端口号不合法");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
    }

}
