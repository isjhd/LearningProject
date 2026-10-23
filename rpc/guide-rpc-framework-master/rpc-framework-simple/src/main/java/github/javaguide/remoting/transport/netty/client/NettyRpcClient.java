package github.javaguide.remoting.transport.netty.client;


import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.enums.ServiceDiscoveryEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.RpcRequestTransport;
import github.javaguide.remoting.transport.netty.codec.RpcMessageCodec;
import github.javaguide.remoting.transport.netty.codec.RpcMessageFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * initialize and close Bootstrap object
 * 初始化 Netty 客户端、服务发现、建立 / 复用长连接、发送 RPC 请求、接收响应、优雅关闭资源。
 * @author shuang.kou
 * @createTime 2020年05月29日 17:51:00
 */
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {

    // 1. 服务发现：从注册中心获取服务端地址（如ZK、Nacos）
    private final ServiceDiscovery serviceDiscovery;
    // 2. 未处理请求池：存储等待响应的RPC请求，实现异步回调
    private final UnprocessedRequests unprocessedRequests;
    // 3. 连接管理器：缓存/复用Netty Channel
    private final ChannelProvider channelProvider;
    // 4. Netty客户端启动器：配置客户端所有参数
    private final Bootstrap bootstrap;
    // 5. Netty IO线程组：处理网络读写、连接事件（核心线程池）
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        // 1. 初始化Netty IO线程组（NIO模式，多线程处理连接）
        eventLoopGroup = new NioEventLoopGroup();
        // 2. RPC消息编解码器（序列化/反序列化
        RpcMessageCodec rpcMessageCodec = new RpcMessageCodec();
        // 3. 初始化Netty客户端启动器
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup) // 绑定IO线程组
                .channel(NioSocketChannel.class) // 指定客户端TCP通道类型
                .handler(new LoggingHandler(LogLevel.INFO)) // Netty日志打印
                // 连接超时时间：5秒（超过则连接失败）
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 通道初始化：配置流水线（Pipeline）的处理器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 心跳机制：5秒内未向服务端发送数据，自动触发心跳
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        // 粘包拆包解码器：解决TCP传输的粘包/拆包问题
                        p.addLast(new RpcMessageFrameDecoder());
                        // RPC消息编解码（序列化+压缩）
                        p.addLast(rpcMessageCodec);
                        // 客户端业务处理器：处理服务端响应
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        // 4. SPI加载服务发现实现（默认ZK）
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        // 5. 单例获取未处理请求池
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        // 6. 单例获取连接管理器
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * connect server and get the channel ,so that you can send rpc message to server
     * 建立连接
     * Netty 连接是异步的，用 CompletableFuture.get() 转为同步阻塞，保证调用方拿到可用连接。
     * @param inetSocketAddress server address
     * @return the channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        // 异步转同步：CompletableFuture接收Netty异步连接结果
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // Netty异步连接服务端
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                // 连接成功：完成Future，返回Channel
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        // 阻塞等待连接完成，返回Channel（异步→同步）
        return completableFuture.get();
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        // 1. 从连接缓存中获取可用Channel
        Channel channel = channelProvider.get(inetSocketAddress);
        // 2. 无缓存/连接无效：新建连接
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            // 3. 新建成功：存入缓存
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 存储响应结果：异步等待服务端返回
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // 1. 服务发现：从注册中心获取服务端IP+端口
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // 2. 获取可用连接（复用/新建）
        Channel channel = getChannel(inetSocketAddress);

        if (channel.isActive()) {
            // 3. 将请求存入未处理池：等待服务端响应后回调
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            // 4. 封装RPC消息：序列化=Hessian，压缩=GZIP，消息类型=请求
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            // 5. 发送消息到服务端
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        // 6. 阻塞等待服务端响应，返回结果
        try {
            return resultFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("rpc请求失败," + e.getMessage());
        }
    }

    public void close() {
        // Netty优雅关闭：释放线程组、关闭所有连接
        eventLoopGroup.shutdownGracefully();
    }
}
