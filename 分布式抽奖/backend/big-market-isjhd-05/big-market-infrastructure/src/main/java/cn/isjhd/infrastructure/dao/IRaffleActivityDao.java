package cn.isjhd.infrastructure.dao;

import cn.isjhd.infrastructure.dao.po.RaffleActivity;
import org.apache.ibatis.annotations.Mapper;


/**
 * @description: 抽奖活动表Dao
 * @author: isjhd
 * @date: 2025/12/1 11:10
 */
@Mapper
public interface IRaffleActivityDao {

    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);

}
