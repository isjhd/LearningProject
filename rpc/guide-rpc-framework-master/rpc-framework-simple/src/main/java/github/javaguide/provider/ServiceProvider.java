package github.javaguide.provider;

import github.javaguide.config.RpcServiceConfig;

/**
 * store and provide service object.
 *
 * @author shuang.kou
 * @createTime 2020年05月31日 16:52:00
 * @description 本地存储服务实例、获取服务实例、对外发布服务
 */
public interface ServiceProvider {

    /**
     * 本地注册服务：将服务实例存储到本地缓存
     * @param rpcServiceConfig 封装了RPC服务的所有配置信息
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务实例：根据服务名从本地缓存中取出服务对象
     * @param rpcServiceName RPC服务唯一名称
     * @return 服务实现类的实例对象
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务：本地注册 + 注册中心注册（对外暴露服务）
     * @param rpcServiceConfig RPC服务配置
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
