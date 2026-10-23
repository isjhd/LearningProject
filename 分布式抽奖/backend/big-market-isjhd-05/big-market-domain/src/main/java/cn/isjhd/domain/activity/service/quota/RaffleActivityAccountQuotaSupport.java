package cn.isjhd.domain.activity.service.quota;

import cn.isjhd.domain.activity.model.entity.ActivityCountEntity;
import cn.isjhd.domain.activity.model.entity.ActivityEntity;
import cn.isjhd.domain.activity.model.entity.ActivitySkuEntity;
import cn.isjhd.domain.activity.adapter.repository.IActivityRepository;
import cn.isjhd.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;

/**
 * @description: 抽奖活动的支撑类
 * @author: isjhd
 * @date: 2025/12/2 14:52
 */
public class RaffleActivityAccountQuotaSupport {

    protected DefaultActivityChainFactory defaultActivityChainFactory;

    protected IActivityRepository activityRepository;

    public RaffleActivityAccountQuotaSupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        this.activityRepository = activityRepository;
        this.defaultActivityChainFactory = defaultActivityChainFactory;
    }

    public ActivitySkuEntity queryActivitySku(Long sku) {
        return activityRepository.queryActivitySku(sku);
    }

    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }

    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }

}
