package cn.isjhd.domain.rebate.repository;

import cn.isjhd.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.isjhd.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.isjhd.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.isjhd.domain.rebate.model.valobj.DailyBehaviorRebateVO;

import java.util.List;

/**
 * @Author: isjhd
 * @Date: 2025-12-15 16:08
 * @Description: 行为返利服务仓储接口
 */
public interface IBehaviorRebateRepository {

    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO);

    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);

    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);

}
