package cn.isjhd.domain.credit.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 11:49
 * @Description: 积分账户实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditAccountEntity {

    /** 用户ID */
    private String userId;

    /** 可用积分，每次扣减的值 */
    private BigDecimal adjustAmount;

}
