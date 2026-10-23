package cn.isjhd.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: isjhd
 * @Date: 2025-12-25 16:38
 * @Description: 商品购物车请求对象
 */
@Data
public class SkuProductShopCartRequestDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * sku 商品
     */
    private Long sku;

}
