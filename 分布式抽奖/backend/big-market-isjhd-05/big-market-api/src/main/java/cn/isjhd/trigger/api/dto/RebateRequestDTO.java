package cn.isjhd.trigger.api.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 返利请求对象
 * @create 2024-10-20 13:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RebateRequestDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 行为类型；sign 签到、openai_pay 支付
     */
    private String behaviorType;
    /**
     * 业务ID；签到则是日期字符串，支付则是外部的业务ID
     */
    private String outBusinessNo;

}
