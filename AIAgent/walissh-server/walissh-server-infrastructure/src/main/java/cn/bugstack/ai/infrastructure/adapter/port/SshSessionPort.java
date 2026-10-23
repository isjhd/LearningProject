package cn.bugstack.ai.infrastructure.adapter.port;

import cn.bugstack.ai.domain.ssh.adapter.port.ISshSessionPort;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH 会话管理器
 * 管理所有活跃的 SSH 连接
 */
@Slf4j
@Component
public class SshSessionPort implements ISshSessionPort {

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final JSch jsch = new JSch();

    /**
     * 建立 SSH 连接
     *
     * @param connectionId 连接ID
     * @param host         主机地址
     * @param port         端口
     * @param username     用户名
     * @param password     密码（密码认证时）
     * @param privateKey   私钥（密钥认证时）
     * @return 是否连接成功
     */
    public boolean connect(String connectionId, String host, int port, String username,
                           String password, String privateKey) {
        // 如果已连接，先断开
        disconnect(connectionId);

        try {
            Session session = jsch.getSession(username, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("ServerAliveInterval", "30");   // 每30秒发送keep-alive
            session.setConfig("ServerAliveCountMax", "3");     // 3次无响应才断开
            session.setTimeout(0); // 不设置socket超时，避免reader线程被误杀

            if (privateKey != null && !privateKey.isEmpty()) {
                // 私钥认证
                jsch.addIdentity(connectionId, privateKey.getBytes(), null, null);
            } else if (password != null && !password.isEmpty()) {
                // 密码认证
                session.setPassword(password);
            } else {
                log.error("SSH连接失败：未提供认证信息 connectionId={}", connectionId);
                return false;
            }

            session.connect();
            sessions.put(connectionId, session);
            log.info("SSH连接成功 connectionId={} host={}:{} user={}", connectionId, host, port, username);
            return true;
        } catch (JSchException e) {
            log.error("SSH连接失败 connectionId={} host={}:{} error={}", connectionId, host, port, e.getMessage());
            return false;
        }
    }

    @Lazy
    @Resource
    private SshFilePort sshFilePort;

    /**
     * 断开 SSH 连接
     *
     * @param connectionId 连接ID
     */
    public void disconnect(String connectionId) {
        log.info("正在断开 SSH 连接 connectionId={}", connectionId);
        
        // 先清理该连接关联的 SFTP channel
        try {
            if (sshFilePort != null) {
                sshFilePort.closeSftp(connectionId);
            }
        } catch (Exception e) {
            log.warn("关闭 SFTP 通道时异常: {}", e.getMessage());
        }
        
        Session session = sessions.remove(connectionId);
        if (session != null) {
            try {
                if (session.isConnected()) {
                    session.disconnect();
                    log.info("SSH连接已断开 connectionId={}", connectionId);
                }
            } catch (Exception e) {
                log.warn("断开 SSH 连接时异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 检查连接是否活跃
     *
     * @param connectionId 连接ID
     * @return 是否已连接
     */
    public boolean isConnected(String connectionId) {
        Session session = sessions.get(connectionId);
        if (session == null) {
            return false;
        }
        try {
            // 不仅检查 isConnected，还要做一个轻量级的实际连接状态检查
            if (session.isConnected()) {
                // 发送一个简单的心跳包检查连接是否真正可用
                session.sendIgnore();
                return true;
            }
        } catch (Exception e) {
            // 出现异常，说明连接实际上已经断开了
            log.warn("检查连接状态时发现异常，连接可能已断开: {}", e.getMessage());
            // 清理无效连接
            try {
                disconnect(connectionId);
            } catch (Exception ex) {
                log.warn("清理无效连接时异常: {}", ex.getMessage());
            }
            return false;
        }
        return false;
    }

    /**
     * 获取会话
     *
     * @param connectionId 连接ID
     * @return JSch Session
     */
    public Session getSession(String connectionId) {
        return sessions.get(connectionId);
    }
}
