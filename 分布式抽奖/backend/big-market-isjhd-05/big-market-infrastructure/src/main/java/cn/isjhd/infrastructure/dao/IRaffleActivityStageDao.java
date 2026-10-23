package cn.isjhd.infrastructure.dao;

import cn.isjhd.infrastructure.dao.po.RaffleActivityStage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动展台 - 上架活动 DAO
 * @create 2024-10-26 17:23
 */
@Mapper
public interface IRaffleActivityStageDao {

    void insert(RaffleActivityStage raffleActivityStage);

    Integer updateStageActivity2ActiveById(Long id);

    Long queryStageActivity2ActiveById(Long id);

    Long queryStageActiveBySC(RaffleActivityStage raffleActivityStage);

    List<RaffleActivityStage> queryStageActivityList();

}
