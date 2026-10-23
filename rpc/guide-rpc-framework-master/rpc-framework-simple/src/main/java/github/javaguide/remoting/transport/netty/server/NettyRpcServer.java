package github.javaguide.remoting.transport.netty.server;

import github.javaguide.config.CustomShutdownHook;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.impl.ZkServiceProviderImpl;
import github.javaguide.remoting.transport.netty.codec.RpcMessageCodec;
import github.javaguide.remoting.transport.netty.codec.RpcMessageFrameDecoder;
import github.javaguide.utils.RuntimeUtil;
import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Server. Receive the client message, call the corresponding method according to the client message,
 * and then return the result to the client.
 * 启动 Netty 服务、注册 RPC 服务到注册中心、处理客户端连接 / 消息、完成 RPC 方法调用并返回结果、优雅关闭资源。
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 16:42:00
 */
@Slf4j
@Component
public class NettyRpcServer {

    // RPC服务端固定端口
    public static final int PORT = 9998;

    // 服务注册器（单例）：负责将服务发布到 ZooKeeper 注册中心
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    // 服务注册方法
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    /**
     * 建立连接事件（Accept）：分发者：Boss Group 里的 EventLoop
     * 1.客户端发起连接 → 操作系统内核通知网卡有新连接
     * 2.NioServerSocketChannel 的 Selector 感知到 连接事件
     * 3.Boss EventLoop 拿到事件 → 分发连接处理逻辑
     * 4.创建客户端专属 SocketChannel
     * 5.把新 Channel 交给 Worker EventLoop 管理
     *
     * 客户端请求事件（Read 读数据）：分发者：Worker Group 里的 EventLoop
     * 1.客户端发送请求数据 → 内核通知有数据可读
     * 2.对应 SocketChannel 的 Selector 感知 读事件
     * 3.Worker EventLoop 拿到事件 → 分发给这个 Channel
     * 4.触发 ChannelPipeline 流水线
     * 5.按顺序分发给：心跳处理器 → 拆包 → 编解码 → 业务处理器
     *
     * 服务端响应事件（Write 写数据）：分发者：Worker Group 里的 EventLoop
     * 1.业务处理器处理完 → 写出响应数据
     * 2.事件反向走 Pipeline
     * 3.到达通道 → Worker EventLoop 分发「写事件」
     * 4.把数据交给内核 → 网卡发送给客户端
     */

    // 启动 Netty 服务
    // 完整执行链路：客户端数据 → 进程 → SocketChannel → 绑定的 IO 线程 → 编解码 → 业务线程池 → 具体业务线程 → 执行业务逻辑
    // Selector：事件监听器，底层监听网卡事件（连接、读、写）。
    // EventLoop：事件分发器，从 Selector 拿事件，分发给对应 Channel。
    // ChannelPipeline：事件流水线，把事件按顺序传给你的业务处理器。
    @SneakyThrows // Lombok：隐藏受检异常，无需手动try-catch
    public void start() {
        // 1. 注册自定义关闭钩子：服务关闭时自动清理资源（服务下线、释放连接）
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        // 2. 获取本机IP地址
        String host = InetAddress.getLocalHost().getHostAddress();

        // 3. Netty IO 线程池：处理网络 IO、编解码、连接事件
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);// 主线程组：仅1个，负责接收客户端连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 工作线程组：默认CPU核心数*2，负责IO读写（解码/编码/数据传输）

        // 4. 业务线程池：处理耗时业务逻辑、异步任务，分离IO线程与业务线程
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,// 线程数：CPU核心数*2
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)// 自定义线程工厂
        );
        try {
            // Netty 服务端启动助手
            ServerBootstrap b = new ServerBootstrap();
            // 共享处理器：全局单例，减少对象创建（线程安全）
            RpcMessageCodec rpcMessageCodec = new RpcMessageCodec();// RPC消息编解码器
            NettyRpcServerHandler nettyRpcServerHandler = new NettyRpcServerHandler();// 业务处理器

            // 核心配置
            b.group(bossGroup, workerGroup)// 绑定主从线程组
                    // 告诉 Netty 服务端使用 NIO 模型接收连接。只监听端口，不处理数据读写。
                    // 监听通道，绑定端口、接收客户端连接（不收发数据）。
                    .channel(NioServerSocketChannel.class)
                    // 禁止攒包，有数据立刻发送。
                    .childOption(ChannelOption.TCP_NODELAY, true) // 配置 客户端连接通道（收发数据），child 管连接
                    // 如果客户端和服务端长时间没有数据交互，系统会自动发送心跳包，检测对方是否还在线。
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 配置 客户端连接通道（收发数据），child 管连接；
                    // TCP 建立连接需要三次握手，SO_BACKLOG 就是这个队列的最大长度。队列满了，后续新连接会被系统直接拒绝。
                    .option(ChannelOption.SO_BACKLOG, 128) // 配置 服务端监听通道（接收连接），option 管监听
                    // Netty 内置日志处理器，专门打印服务端监听端口的所有生命周期日志（绑定端口、接收连接、异常等）。
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 给「每个客户端连接的子通道」绑定业务处理流水线
                    .childHandler(
                            // 创建通道初始化器，指定处理【客户端连接通道 SocketChannel】
                            // SocketChannel：数据通道，和客户端收发数据、执行业务逻辑。一个客户端连接 = 一个独立的 SocketChannel
                            new ChannelInitializer<SocketChannel>() {
                                // 新连接创建时，自动调用这个方法！
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    // 获取当前连接的专属流水线
                                    ChannelPipeline p = ch.pipeline();
                                    // 1. 空闲检测：30秒未收到客户端读请求，自动关闭连接
                                    p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                                    // 2. 解决TCP粘包/拆包：自定义帧解码器
                                    p.addLast(new RpcMessageFrameDecoder());
                                    // 3. RPC消息编解码：序列化/反序列化
                                    p.addLast(rpcMessageCodec);
                                    // 4. 业务处理器：执行RPC方法调用（绑定业务线程池）
                                    p.addLast(serviceHandlerGroup, nettyRpcServerHandler);
                                }
                            });

            // bind(host, PORT)：将服务端的 父通道（NioServerSocketChannel） 绑定到指定的 IP 地址 + 端口号，并开始监听客户端连接。
            // sync()：主线程会停下来等待，直到端口绑定成功 / 绑定失败。
            ChannelFuture f = b.bind(host, PORT).sync();
            // .channel()：获取服务端监听通道（父通道 NioServerSocketChannel）
            // .closeFuture()：获取通道的关闭监听器。
            // .sync()：主线程会停在这里，直到通道关闭（手动停止服务、程序崩溃），才会往下走。
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            // 优雅关闭所有线程组：释放线程、端口、连接资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }


}
