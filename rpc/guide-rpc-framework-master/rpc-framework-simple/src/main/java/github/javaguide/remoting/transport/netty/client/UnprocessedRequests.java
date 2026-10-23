package github.javaguide.remoting.transport.netty.client;

import github.javaguide.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * unprocessed requests by the server.
 * 存储客户端已发送但未收到服务端响应的 RPC 请求
 *
 * @author shuang.kou
 * @createTime 2020年06月04日 17:30:00
 */
public class UnprocessedRequests {

    // 线程安全的Map：key=请求唯一ID(requestId)，value=存储响应结果的异步Future
    // 彻底解决多并发请求下，响应找不到对应请求的问题。
    // 解决「异步响应结果托管」的问题
    // 解决「异步网络 → 同步业务调用」的核心问题
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    /**
     * 发送RPC请求时调用：将请求存入未处理池
     * @param requestId  请求唯一标识
     * @param future     用于接收服务端响应的Future
     */
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    /**
     * 接收RPC响应时调用：完成异步Future，返回结果给调用方
     * @param rpcResponse 服务端返回的响应
     */
    public void complete(RpcResponse<Object> rpcResponse) {
        // 1. 根据requestId取出Future，并立即从Map中移除（防止内存泄漏）
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        // 2. 找到对应的Future，赋值响应结果
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
