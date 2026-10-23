package cn.isjhd.domain.credit.repository;

import cn.isjhd.domain.credit.model.aggregate.TradeAggregate;
import cn.isjhd.domain.credit.model.entity.CreditAccountEntity;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 11:32
 * @Description: 用户积分仓储
 */
public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

    CreditAccountEntity queryUserCreditAccount(String userId);

}
