package github.javaguide.provider.impl;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.enums.ServiceRegistryEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shuang.kou
 * @createTime 2020年05月13日 11:23:00
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    // 本地服务缓存：key=服务唯一名称，value=服务实现类实例
    private final Map<String, Object> serviceMap;
    // 已注册服务集合：防止重复注册服务
    private final Set<String> registeredService;
    // 服务注册中心接口：SPI加载的ZK注册中心实现类
    private final ServiceRegistry serviceRegistry;


    public ZkServiceProviderImpl() {
        // 初始化线程安全的服务缓存
        serviceMap = new ConcurrentHashMap<>();
        // 初始化线程安全的已注册服务集合
        registeredService = ConcurrentHashMap.newKeySet();
        // SPI机制加载 ZK 注册中心扩展实现
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        // 获取服务唯一名称
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        // 防重复注册：已注册直接返回
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        // 标记为已注册
        registeredService.add(rpcServiceName);
        // 存入本地服务缓存
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        // 从本地缓存获取服务实例
        Object service = serviceMap.get(rpcServiceName);
        // 服务不存在：抛自定义异常
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            // 获取本机IP地址
            String host = InetAddress.getLocalHost().getHostAddress();
            // 第一步：本地注册服务（缓存）
            this.addService(rpcServiceConfig);
            // 第二步：将【服务名 + 本机IP:端口】注册到ZK注册中心
            serviceRegistry.registerService(
                    rpcServiceConfig.getRpcServiceName(),
                    new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

}
