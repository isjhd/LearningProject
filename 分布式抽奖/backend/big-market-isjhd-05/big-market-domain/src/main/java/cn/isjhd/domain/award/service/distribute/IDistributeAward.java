package cn.isjhd.domain.award.service.distribute;

import cn.isjhd.domain.award.model.entity.DistributeAwardEntity;

/**
 * @Author: isjhd
 * @Date: 2025-12-23 10:51
 * @Description: 分发奖品接口
 */
public interface IDistributeAward {

    void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) throws Exception;

}
