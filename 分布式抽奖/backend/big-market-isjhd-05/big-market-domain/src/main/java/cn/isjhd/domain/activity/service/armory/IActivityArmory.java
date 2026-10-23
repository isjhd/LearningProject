package cn.isjhd.domain.activity.service.armory;

/**
 * @description: 活动装配预热
 * @author: isjhd
 * @date: 2025/12/3 16:15
 */
public interface IActivityArmory {

    boolean assembleActivitySkuByActivityId(Long activityId);

    boolean assembleActivitySku(Long sku);

}
