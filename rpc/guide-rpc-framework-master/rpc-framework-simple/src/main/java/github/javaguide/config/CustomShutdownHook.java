package github.javaguide.config;

import github.javaguide.registry.zk.util.CuratorUtils;
import github.javaguide.remoting.transport.netty.server.NettyRpcServer;
import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author shuang.kou
 * @createTime 2020年06月04日 13:11:00
 * @description JVM 正常关闭时，完成 服务注销 + 线程池关闭 双核心清理；
 * 单例设计 + 容错处理，完全适配分布式环境。
 */
@Slf4j
public class CustomShutdownHook {

    // 全局只需要 一个关闭钩子，避免重复注册导致多次清理资源；
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();
    // 公有静态方法：获取单例对象
    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    /**
     * 什么是 JVM 关闭钩子？
     * Runtime.getRuntime().addShutdownHook(Thread) 是 Java 原生 API：
     * 作用：在 JVM 退出前，执行资源清理、服务下线等收尾工作。
     *
     * 触发时机：JVM 正常关闭时自动执行（如：Ctrl+C、程序正常退出、kill -15 PID）；
     * 不触发：kill -9 PID 强制杀死进程、JVM 崩溃；
     */

    // 核心方法：注册JVM关闭钩子，执行资源清理
    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        // 关键API：向JVM注册一个关闭钩子线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 构建本机IP + Netty服务端口的地址对象
                InetSocketAddress inetSocketAddress = new InetSocketAddress(
                        InetAddress.getLocalHost().getHostAddress(),
                        NettyRpcServer.PORT);
                // 注销 ZooKeeper 服务
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
                // 忽略本机IP获取异常（极端情况才会出现，不影响主逻辑）
            }
            // 关闭所有线程池
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}
