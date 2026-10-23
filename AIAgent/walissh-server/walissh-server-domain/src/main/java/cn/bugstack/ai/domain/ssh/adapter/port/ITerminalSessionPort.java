package cn.bugstack.ai.domain.ssh.adapter.port;

/**
 * 终端会话服务接口
 * 负责管理 SSH 终端会话，包括打开/写入/读取/调整大小/关闭会话
 *
 * @author waissh dev
 */
public interface ITerminalSessionPort {

    /**
     * 打开终端会话
     *
     * @param connectionId SSH连接ID
     * @param cols         终端列数
     * @param rows         终端行数
     * @return 会话ID
     */
    String openTerminal(String connectionId, int cols, int rows);

    /**
     * 写入命令到终端
     *
     * @param sessionId 会话ID
     * @param command   命令内容
     */
    void write(String sessionId, String command);

    /**
     * 读取终端输出
     *
     * @param sessionId 会话ID
     * @return 终端输出内容
     */
    String read(String sessionId);

    /**
     * 为 Agent 专用读取开启/关闭捕获模式
     * 开启后，输出会被同时写入主缓冲区和 agent 专用缓冲区
     *
     * @param sessionId 会话ID
     * @param capture   true=开启捕获, false=关闭捕获
     */
    void setAgentCapture(String sessionId, boolean capture);

    /**
     * 读取 Agent 专用缓冲区内容（并清空）
     *
     * @param sessionId 会话ID
     * @return Agent 专用缓冲区内容
     */
    String readAgentBuffer(String sessionId);

    /**
     * 调整终端大小
     *
     * @param sessionId 会话ID
     * @param cols      新的列数
     * @param rows      新的行数
     */
    void resize(String sessionId, int cols, int rows);

    /**
     * 关闭终端会话
     *
     * @param sessionId 会话ID
     */
    void closeSession(String sessionId);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean sessionExists(String sessionId);

}