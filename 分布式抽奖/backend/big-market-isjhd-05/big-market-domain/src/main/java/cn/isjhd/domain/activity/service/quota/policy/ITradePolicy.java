package cn.isjhd.domain.activity.service.quota.policy;

import cn.isjhd.domain.activity.model.aggregate.CreateQuotaOrderAggregate;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 16:24
 * @Description: 交易策略接口，包括；返利兑换（不用支付），积分订单（需要支付）
 */
public interface ITradePolicy {

    void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate);

}
