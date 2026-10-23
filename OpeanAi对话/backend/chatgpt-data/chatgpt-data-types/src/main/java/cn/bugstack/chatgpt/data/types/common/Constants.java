package cn.bugstack.chatgpt.data.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 21:24
 */
public class Constants {

    public final static String SPLIT = ",";

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode {
        SUCCESS("0000", "成功"),
        UN_ERROR("0001", "未知失败"),
        ILLEGAL_PARAMETER("0002", "非法参数"),
        TOKEN_ERROR("0003", "登录权限拦截"),
        APP_TOKEN_ERROR("0004", "接口权限拦截"),
        ORDER_PRODUCT_ERR("OE001", "所购商品已下线，请重新选择下单商品"),
        ;

        private String code;
        private String info;

    }

}
