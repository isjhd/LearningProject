package cn.isjhd.domain.activity.adapter.repository;

import cn.isjhd.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.isjhd.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.isjhd.domain.activity.model.entity.*;
import cn.isjhd.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: 活动仓储接口
 * @author: isjhd 活动仓储接口
 * @date: 2025/12/1 16:37
 */

public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveNoPayOrder(CreateQuotaOrderAggregate createOrderAggregate);

    void doSaveCreditPayOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);

    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    ActivitySkuStockKeyVO takeQueueValue(Long sku);

    void clearQueueValue();

    void clearQueueValue(Long sku);

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    List<Long> querySkuList();

    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day);

    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    List<ActivitySkuEntity> queryActivitySkuListByActivityId(Long activityId);

    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);

    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);

    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);

    UnpaidActivityOrderEntity queryUnpaidActivityOrder(SkuRechargeEntity skuRechargeEntity);

    List<SkuProductEntity> querySkuProductEntityListByActivityId(Long activityId);

    BigDecimal queryUserCreditAccountAmount(String userId);

    void appendStageActivity(String channel, String source, Long activityId);

    void updateStageActivity2Active(Long id);

    Long queryStageActiveBySC(String channel, String source);

    List<RaffleActivityStageEntity> queryStageActivityList();

    Long queryStageActivity2ActiveById(Long id);

}
