package cn.isjhd.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 抽奖请求参数
 * @author: isjhd
 * @date: 2025/11/24 15:01
 */
@Data
public class RaffleStrategyRequestDTO implements Serializable {

    // 抽奖策略ID
    private Long strategyId;

}
