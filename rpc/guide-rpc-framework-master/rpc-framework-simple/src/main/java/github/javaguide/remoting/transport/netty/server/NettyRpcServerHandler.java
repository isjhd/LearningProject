package github.javaguide.remoting.transport.netty.server;

import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.RpcResponseCodeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Customize the ChannelHandler of the server to process the data sent by the client.
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * Netty 服务端核心入站处理器，直接对接客户端的网络消息，负责处理心跳、解析 RPC 请求、调用本地服务、返回响应、空闲连接管理、异常处理
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 20:44:00
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    // RPC请求实际执行器：负责反射调用本地服务的目标方法
    private final RpcRequestHandler rpcRequestHandler;

    // 构造方法：单例模式获取请求处理器
    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }


    /**
     * 服务端接收所有客户端数据的入口
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 仅处理自定义的 RpcMessage 消息
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();

                // 初始化响应消息：固定使用 Hessian 序列化 + GZIP 压缩
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());

                // 处理心跳请求
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);// 客户端发PING，服务端回PONG
                }
                // 处理RPC业务请求
                else {
                    // 提取RPC请求参数（接口名、方法名、参数、请求ID等）
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // 反射调用本地服务方法，获取执行结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));

                    // 设置响应类型为 RPC 响应
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);

                    // 判断通道是否可用，封装成功/失败响应
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                // 发送响应消息，发送失败则自动关闭连接
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // 强制释放消息，防止 Netty 内存泄漏（关键！）
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 接收 Netty 框架 / 自定义的事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断是否为空闲事件
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 读空闲：30秒未收到客户端消息（对应服务端配置的 IdleStateHandler）
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();// 关闭无效连接，释放资源
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();// 发生异常，立即关闭连接
    }
}
