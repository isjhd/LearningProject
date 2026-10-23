package github.javaguide.registry;

import github.javaguide.extension.SPI;
import github.javaguide.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * service discovery
 * 服务发现
 *
 * @author shuang.kou
 * @createTime 2020年06月01日 15:16:00
 */
@SPI
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     * 根据RPC服务名查找服务
     *
     * @param rpcRequest RPC请求实体对象
     * @return 服务地址
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
