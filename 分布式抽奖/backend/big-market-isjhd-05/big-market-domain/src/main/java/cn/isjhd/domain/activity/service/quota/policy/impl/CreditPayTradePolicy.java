package cn.isjhd.domain.activity.service.quota.policy.impl;

import cn.isjhd.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.isjhd.domain.activity.model.valobj.OrderStateVO;
import cn.isjhd.domain.activity.adapter.repository.IActivityRepository;
import cn.isjhd.domain.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 16:25
 * @Description: 积分兑换，支付类订单
 */
@Service("credit_pay_trade")
public class CreditPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public CreditPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        createQuotaOrderAggregate.setOrderState(OrderStateVO.wait_pay);
        activityRepository.doSaveCreditPayOrder(createQuotaOrderAggregate);
    }

}
