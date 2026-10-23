package github.javaguide.remoting.handler;

import github.javaguide.exception.RpcException;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.impl.ZkServiceProviderImpl;
import github.javaguide.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RpcRequest processor
 * 接收客户端发来的 RpcRequest 请求，通过反射调用服务端对应的目标方法，并返回执行结果。
 * @author shuang.kou
 * @createTime 2020年05月13日 09:05:00
 */
@Slf4j
public class RpcRequestHandler {

    // 服务提供者：负责从注册中心获取服务实例
    private final ServiceProvider serviceProvider;

    // 构造方法：通过单例工厂初始化 基于ZooKeeper的服务提供者
    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * Processing rpcRequest: call the corresponding method, and then return the method
     * 处理RPC请求：调用对应方法并返回结果
     */
    public Object handle(RpcRequest rpcRequest) {
        // 1. 根据请求中的服务名，从注册中心获取服务实例
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        // 2. 反射调用目标方法
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 反射执行目标方法，获取执行结果
     * @param rpcRequest 客户端请求
     * @param service    服务实例对象
     * @return 方法执行结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            // 1. 通过【方法名 + 参数类型】获取目标Method对象
            Method method = service.getClass().getMethod(
                    rpcRequest.getMethodName(),
                    rpcRequest.getParamTypes());
            // 2. 反射调用方法：传入服务实例 + 方法参数
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
