package github.javaguide.registry.zk;

import github.javaguide.enums.LoadBalanceEnum;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.loadbalance.LoadBalance;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.registry.zk.util.CuratorUtils;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * service discovery based on zookeeper
 * 基于Zookeeper的服务发现实现
 *
 * @author shuang.kou
 * @createTime 2020年06月01日 15:16:00
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    // 负载均衡接口
    private final LoadBalance loadBalance;

    // 构造方法：通过SPI机制加载负载均衡实现类
    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCENEW.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        // 1. 从RPC请求中获取【服务唯一名称】（ZK上的节点路径）
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 2. 获取Zookeeper客户端（单例，工具类封装）
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 3. 从ZK获取该服务下的所有提供者地址列表（子节点：ip:port）
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);

        // 4. 判空：无服务可用，抛出自定义异常
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }

        // 5. 负载均衡：从地址列表中选择一个目标地址（核心！）
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);

        // 6. 拆分地址：把 "ip:port" 拆分为主机和端口
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);

        // 7. 返回Java标准网络地址对象，供消费者发起RPC调用
        return new InetSocketAddress(host, port);
    }
}
