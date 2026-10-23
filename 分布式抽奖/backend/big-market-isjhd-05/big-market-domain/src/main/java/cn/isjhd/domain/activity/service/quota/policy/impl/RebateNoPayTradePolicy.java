package cn.isjhd.domain.activity.service.quota.policy.impl;

import cn.isjhd.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.isjhd.domain.activity.model.valobj.OrderStateVO;
import cn.isjhd.domain.activity.adapter.repository.IActivityRepository;
import cn.isjhd.domain.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 16:27
 * @Description: 返利无支付交易订单，直接充值到账
 */
@Service("rebate_no_pay_trade")
public class RebateNoPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public RebateNoPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        // 不需要支付则修改订单金额为0，状态为完成，直接给用户账户充值
        createQuotaOrderAggregate.setOrderState(OrderStateVO.completed);
        createQuotaOrderAggregate.getActivityOrderEntity().setPayAmount(BigDecimal.ZERO);
        activityRepository.doSaveNoPayOrder(createQuotaOrderAggregate);
    }
}
