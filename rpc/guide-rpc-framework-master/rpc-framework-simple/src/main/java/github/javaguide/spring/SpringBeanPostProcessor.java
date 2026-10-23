package github.javaguide.spring;

import github.javaguide.annotation.RpcReference;
import github.javaguide.annotation.RpcService;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcRequestTransportEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.impl.ZkServiceProviderImpl;
import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * call this method before creating the bean to see if the class is annotated
 * 服务端：Bean 初始化前 → 自动将 @RpcService 服务发布到注册中心（Zookeeper）；
 * 客户端：Bean 初始化后 → 自动为 @RpcReference 字段注入动态代理对象，实现远程调用。
 * @author shuang.kou
 * @createTime 2020年07月14日 16:42:00
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    // 服务发布器：负责将服务注册到 ZK 注册中心
    private final ServiceProvider serviceProvider;
    // RPC 客户端传输器：负责网络通信（Netty）
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        // 1. 单例工厂获取 ZK 服务发布器（全局唯一）
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        // 2. SPI 机制加载 Netty 传输客户端（可扩展：HTTP、gRPC 等）
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(RpcRequestTransportEnum.NETTY.getName());
    }


    /**
     * 对带有@RpcSerivce注解的类进行代理，直接将类发布到ZK上
     * 注意是在初始化之前，将类注册到zk上
     *
     * Bean 只要实例化 + 属性填充完，就是一个完整可用的服务，不需要等初始化方法执行，越早注册到 ZK，消费者越早能发现。
     * */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 判断：当前 Bean 是否标注 @RpcService 注解
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());

            // 1. 获取注解信息（group、version：服务分组/版本，用于区分服务）
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);

            // 2. 封装服务配置（服务实例 + 分组 + 版本）
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();

            // 3. 核心：将服务发布到 ZK 注册中心
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * 为标注了@RpcReference注解的字段动态生成代理对象，从而实现远程服务的调用
     *
     * 必须等 Bean 完全初始化完毕，再注入远程代理，不会干扰 Bean 自身的初始化逻辑，最安全。
     * */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        // 遍历 Bean 的所有字段
        Field[] declaredFields = targetClass.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            // 判断：字段是否标注 @RpcReference 注解
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 1. 封装远程服务配置（分组、版本）
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();

                // 2. 创建 RPC 动态代理工厂
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                // 3. 为远程接口生成动态代理对象
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    // 4. 反射暴力访问，将代理对象注入到字段中
                    // 反射赋值时，JVM 会识别对象的真实类型（实现了目标接口），直接通过。
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
