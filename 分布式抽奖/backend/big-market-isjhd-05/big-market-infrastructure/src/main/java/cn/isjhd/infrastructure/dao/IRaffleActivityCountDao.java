package cn.isjhd.infrastructure.dao;

import cn.isjhd.infrastructure.dao.po.RaffleActivityCount;
import org.apache.ibatis.annotations.Mapper;


/**
 * @description: 抽奖活动次数配置表Dao
 * @author: isjhd
 * @date: 2025/12/1 11:10
 */
@Mapper
public interface IRaffleActivityCountDao {

    RaffleActivityCount queryRaffleActivityCountByActivityCountId(Long activityCountId);

}
