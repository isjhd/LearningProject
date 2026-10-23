package cn.isjhd.domain.credit.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 11:47
 * @Description: 交易名称枚举值
 */
@Getter
@AllArgsConstructor
public enum TradeNameVO {

    REBATE("行为返利"),
    CONVERT_SKU("兑换抽奖"),
            ;

    private final String name;

}
