package cn.isjhd.trigger.api;

import cn.isjhd.trigger.api.dto.*;
import cn.isjhd.trigger.api.response.Response;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: isjhd
 * @Date: 2025-12-10 14:37
 * @Description: 抽奖活动服务
 */
public interface IRaffleActivityService {

    /**
     * 根据sc值，查询上架的活动ID
     *
     * @param channel 渠道
     * @param source  来源
     * @return 活动ID
     */
    Response<Long> queryStageActivityId(String channel, String source);

    /**
     * 活动装配，数据预热缓存
     *
     * @param activityId 活动ID
     * @return 装配结果
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 活动抽奖接口
     *
     * @param request 请求对象
     * @return 返回结果
     */
    Response<ActivityDrawResponseDTO> draw(String token, ActivityDrawRequestDTO request);

    /**
     * 活动抽奖接口
     *
     * @param request 请求对象
     * @return 返回结果
     */
    Response<ActivityDrawResponseDTO> draw(ActivityDrawRequestDTO request);

    /**
     * 日历签到返利接口
     *
     * @param token 登录Token
     * @return 签到结果
     */
    Response<Boolean> calendarSignRebateByToken(String token);

    /**
     * 日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    Response<Boolean> calendarSignRebate(String userId);

    /**
     * 判断是否完成日历签到返利接口
     *
     * @param token token
     * @return 签到结果 true 已签到，false 未签到
     */
    Response<Boolean> isCalendarSignRebateByToken(String token);

    /**
     * 判断是否完成日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到结果 true 已签到，false 未签到
     */
    Response<Boolean> isCalendarSignRebate(String userId);

    /**
     * 查询用户活动账户
     *
     * @param token   鉴权token
     * @param request 请求对象「活动ID、用户ID」
     * @return 返回结果「总额度、月额度、日额度」
     */
    Response<UserActivityAccountResponseDTO> queryUserActivityAccount(String token, UserActivityAccountRequestDTO request);

    /**
     * 查询用户活动账户
     *
     * @param request 请求对象「活动ID、用户ID」
     * @return 返回结果「总额度、月额度、日额度」
     */
    Response<UserActivityAccountResponseDTO> queryUserActivityAccount(UserActivityAccountRequestDTO request);

    /**
     * 查询sku商品集合
     *
     * @param activityId 活动ID
     * @return 商品集合
     */
    Response<List<SkuProductResponseDTO>> querySkuProductListByActivityId(Long activityId);

    Response<BigDecimal> queryUserCreditAccountByToken(String token);

    /**
     * 查询用户积分值
     *
     * @param userId 用户ID
     * @return 可用积分
     */
    Response<BigDecimal> queryUserCreditAccount(String userId);

    Response<Boolean> creditPayExchangeSku(String token, SkuProductShopCartRequestDTO request);

    /**
     * 积分支付兑换商品
     *
     * @param request 请求对象「用户ID、商品ID」
     * @return 兑换结果
     */
    Response<Boolean> creditPayExchangeSku(SkuProductShopCartRequestDTO request);

}
