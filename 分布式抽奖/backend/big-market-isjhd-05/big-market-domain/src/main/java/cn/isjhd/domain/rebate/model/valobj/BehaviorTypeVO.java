package cn.isjhd.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: isjhd
 * @Date: 2025-12-15 15:53
 * @Description: 行为类型枚举值对象
 */
@Getter
@AllArgsConstructor
public enum BehaviorTypeVO {

    SIGN("SIGN", "签到（日历）"),
    OPENAI_PAY("OPENAI_PAY", "openai 外部支付完成"),
    ;

    private final String code;
    private final String info;

}
