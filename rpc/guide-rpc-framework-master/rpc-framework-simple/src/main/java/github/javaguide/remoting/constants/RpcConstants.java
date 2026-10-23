package github.javaguide.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 这是一个纯常量类（无业务逻辑、无状态）。
 * 专门存放RPC 通信协议、消息、心跳、网络传输的所有固定值。
 *
 * @author wangtao .
 * @createTime on 2020/10/2
 */
public class RpcConstants {


    /**
     * Magic number. Verify RpcMessage
     * 所有自定义网络协议（如 Redis、MySQL）都有魔法数，用于身份校验。
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    // 全局统一使用 UTF-8 编码字符串
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 协议版本
    public static final byte VERSION = 1;

    // 固定头部长度
    public static final byte TOTAL_LENGTH = 16;
    public static final int HEAD_LENGTH = 16;

    // 业务请求类型
    public static final byte REQUEST_TYPE = 1;
    // 业务响应类型
    public static final byte RESPONSE_TYPE = 2;

    // 心跳请求（ping）
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    // 心跳响应（pong）
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    // 客户端空闲 → 发送 PING
    public static final String PING = "ping";
    // 服务端收到 → 回复 PONG
    public static final String PONG = "pong";

    // 单条 RPC 消息最大限制：8MB
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
