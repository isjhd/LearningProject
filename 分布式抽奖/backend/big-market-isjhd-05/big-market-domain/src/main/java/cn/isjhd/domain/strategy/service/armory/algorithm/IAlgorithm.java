package cn.isjhd.domain.strategy.service.armory.algorithm;

import cn.isjhd.domain.strategy.model.entity.StrategyAwardEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: isjhd
 * @Date: 2026-01-23 11:21
 * @Description: 抽奖算法
 */
public interface IAlgorithm {

    void armoryAlgorithm(String key, List<StrategyAwardEntity> strategyAwardEntities, BigDecimal rateRange);

    Integer dispatchAlgorithm(String key);

}
