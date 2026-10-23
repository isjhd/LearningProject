package cn.isjhd.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 策略奖品库存Key标识值对象
 * @author: isjhd
 * @date: 2025/11/22 15:18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardStockKeyVO {

    //策略ID
    private Long strategyId;
    //奖品ID
    private Integer awardId;

}
