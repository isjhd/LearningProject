package cn.isjhd.domain.award.service;

import cn.isjhd.domain.award.model.entity.DistributeAwardEntity;
import cn.isjhd.domain.award.model.entity.UserAwardRecordEntity;

/**
 * @Author: isjhd
 * @Date: 2025-12-09 12:37
 * @Description: 奖品服务接口
 */

public interface IAwardService {

    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);

    /**
     * 配送发货奖品
     */
    void distributeAward(DistributeAwardEntity distributeAwardEntity) throws Exception;


}
