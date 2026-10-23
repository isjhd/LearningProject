package cn.isjhd.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 17:28
 * @Description: 出货单实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryOrderEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 业务防重ID - 外部透传，返利、行为等唯一标识
     */
    private String outBusinessNo;

}
