package github.javaguide.loadbalance;

import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.utils.CollectionUtil;

import java.util.List;

/**
 * 负载均衡策略的抽象类
 *
 * @author shuang.kou
 * @createTime 2020年06月21日 07:44:00
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 1. 判空
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        // 2. 只有1个服务节点，无需算法，直接返回
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        // 3. 使用钩子函数，进行选择
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

}
