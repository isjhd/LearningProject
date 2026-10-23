package github.javaguide.loadbalance;

import github.javaguide.extension.SPI;
import github.javaguide.remoting.dto.RpcRequest;

import java.util.List;

/**
 * Interface to the load balancing policy
 *
 * @author shuang.kou
 * @createTime 2020年06月21日 07:44:00
 * @description 负载均衡策略
 */
@SPI // 标记为SPI扩展点，支持动态加载不同实现类
public interface LoadBalance {
    /**
     * 从服务地址列表中选择一个目标地址
     *
     * @param serviceUrlList 服务地址列表（所有可用节点）
     * @param rpcRequest RPC请求对象
     * @return 最终选中的服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
