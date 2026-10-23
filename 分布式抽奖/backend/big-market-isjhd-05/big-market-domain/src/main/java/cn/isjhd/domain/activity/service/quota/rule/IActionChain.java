package cn.isjhd.domain.activity.service.quota.rule;

import cn.isjhd.domain.activity.model.entity.ActivityCountEntity;
import cn.isjhd.domain.activity.model.entity.ActivityEntity;
import cn.isjhd.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @description: 下单规则过滤接口
 * @author: isjhd
 * @date: 2025/12/2 15:06
 */
public interface IActionChain extends IActionChainArmory {

    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}
