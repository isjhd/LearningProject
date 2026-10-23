package cn.isjhd.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 抽奖奖品列表，请求对象
 * @author: isjhd
 * @date: 2025/11/24 14:41
 */
@Data
public class RaffleAwardListRequestDTO implements Serializable {

    // 用户ID
    private String userId;
    // 抽奖活动ID
    private Long activityId;

}
