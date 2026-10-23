package github.javaguide.remoting.dto;

import github.javaguide.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @author shuang.kou
 * @createTime 2020年05月12日 16:15:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {

    // 固定序列化 ID：防止 JVM 反序列化时因类修改导致 InvalidClassException。
    private static final long serialVersionUID = 715745410605631233L;
    // 请求 ID → 和 RpcRequest.requestId 完全一致
    private String requestId;
    /**
     * 响应状态码 → 标识成功 / 失败
     */
    private Integer code;
    /**
     * 响应描述信息 → 成功提示 / 失败原因
     */
    private String message;
    /**
     * 响应数据 → 业务方法的实际返回值
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        // 设置成功状态码和信息（从枚举获取）
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        // 绑定请求ID，用于客户端匹配
        response.setRequestId(requestId);
        // 赋值业务数据
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        // 设置失败状态码和信息
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

}
