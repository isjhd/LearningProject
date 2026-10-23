package github.javaguide.registry.zk;

import github.javaguide.registry.ServiceRegistry;
import github.javaguide.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * service registration  based on zookeeper
 *  Zookeeper 服务注册实现类
 *
 * @author shuang.kou
 * @createTime 2020年05月31日 10:56:00
 */
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {

    /**
     * 重写服务注册核心方法：将服务名+地址注册到Zookeeper
     * @param rpcServiceName    RPC服务唯一名称（接口全类名）
     * @param inetSocketAddress 服务提供者的IP+端口
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        // 1. 拼接Zookeeper上的服务节点路径
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        // 2. 获取单例的Zookeeper客户端（工具类封装）
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 3. 在Zookeeper中创建【持久节点】，完成服务注册
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
