package cn.isjhd.domain.award.adapter.repository;

import cn.isjhd.domain.award.model.aggregate.GiveOutPrizesAggregate;
import cn.isjhd.domain.award.model.aggregate.UserAwardRecordAggregate;

/**
 * @Author: isjhd
 * @Date: 2025-12-09 14:38
 * @Description: 奖品仓储服务
 */

public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

    String queryAwardConfig(Integer awardId);

    void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate);

    String queryAwardKey(Integer awardId);

}
