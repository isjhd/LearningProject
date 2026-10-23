package github.javaguide.remoting.dto;


import lombok.*;

/**
 * RPC 框架的消息实体类，封装了通信协议 + 业务数据
 * @author wangtao
 * @createTime 2020年10月2日 12:33
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * rpc message type
     * 消息类型：区分请求 / 响应 / 心跳包
     */
    private byte messageType;
    /**
     * serialization type
     * 序列化类型：指定数据序列化方式。比如JSON格式
     */
    private byte codec;
    /**
     * compress type
     * 压缩类型：指定数据压缩算法
     */
    private byte compress;
    /**
     * request id
     * 请求 ID：全局唯一 ID，用于异步匹配请求和响应
     */
    private int requestId;
    /**
     * request data
     * 消息体数据：承载实际业务数据
     */
    private Object data;

}
