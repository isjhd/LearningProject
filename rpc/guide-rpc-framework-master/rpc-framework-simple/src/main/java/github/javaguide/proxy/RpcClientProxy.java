package github.javaguide.proxy;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.enums.RpcResponseCodeEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is like calling the local method (the intermediate process is shielded)
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 19:01:00
 * @description 让客户端调用远程服务方法，和调用本地方法完全一样
 */
@Slf4j
// InvocationHandler
// 当你调用代理对象的任意方法时，不会直接执行目标方法，
// 而是自动跳转到 invoke() 方法中执行你自定义的逻辑。
public class RpcClientProxy implements InvocationHandler {// 实现JDK动态代理核心接口

    // 异常信息拼接常量
    private static final String INTERFACE_NAME = "interfaceName";

    /** 发送网络请求的工具，对接服务端 */
    private final RpcRequestTransport rpcRequestTransport;
    /** 服务配置：存储服务分组、版本，用于微服务寻址 */
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }


    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * get the proxy object
     */
    @SuppressWarnings("unchecked")// 抑制泛型强转警告
    public <T> T getProxy(Class<T> clazz) {
        // JDK动态代理核心方法：生成代理对象
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(), // 1. 目标接口的类加载器
                new Class<?>[]{clazz},  // 2. 要代理的接口（JDK只能代理接口）
                this);                  // 3. InvocationHandler实例（当前类）
    }

    /**
     *
     * @param proxy 生成的代理对象本身
     * @param method 客户端实际调用的目标方法
     * @param args 调用方法时传入的参数数组
     *
     * @return
     */
    @SneakyThrows // Lombok：自动抛出受检异常，无需try-catch
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1. 创建一个PRC请求
        log.info("invoked method: [{}]", method.getName());

        // 2. 构建RPC请求实体（封装所有调用信息）
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())// 方法名
                .parameters(args)// 方法参数
                .interfaceName(method.getDeclaringClass().getName())// 接口全类名
                .paramTypes(method.getParameterTypes())// 参数类型
                .requestId(UUID.randomUUID().toString())// 唯一请求ID（匹配响应）
                .group(rpcServiceConfig.getGroup())// 服务分组
                .version(rpcServiceConfig.getVersion())// 服务版本
                .build();
        // 3. 发送RPC请求到服务端，获取响应
        RpcResponse<Object> rpcResponse =  (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);

        // 4. 校验响应合法性
        this.check(rpcResponse, rpcRequest);

        // 5. 返回服务端的执行结果
        // 泛型是语法糖 → 编译后泛型被擦除，但接口声明的 String 返回值让编译器自动做了类型匹配，所以你不用写强转。
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        // 校验1：响应为空 → 请求发送失败
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        // 校验2：请求ID与响应ID不匹配 → 网络乱序
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        // 校验3：响应码不是成功 → 服务端执行失败
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
