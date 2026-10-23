package cn.bugstack.chatgpt.data.infrastructure.adapter.port;

import cn.bugstack.chatgpt.data.domain.order.adapter.port.IOrderPort;
import cn.bugstack.chatgpt.data.infrastructure.gateway.RebateServiceRPC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 适配端口
 * @create 2024-10-20 14:51
 */
@Service
public class OrderPort implements IOrderPort {

    @Resource
    private RebateServiceRPC rebateServiceRPC;

    @Override
    public void rebate(String userId, String orderId) {
        rebateServiceRPC.rebate(userId, orderId);
    }

}
