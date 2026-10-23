package cn.bugstack.chatgpt.data.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAi 产品持久化对象
 * @create 2023-10-05 10:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenAIProductPO {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 商品ID
     */
    private Integer productId;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 商品描述
     */
    private String productDesc;
    /**
     * 额度次数
     */
    private Integer quota;
    /**
     * 商品价格
     */
    private BigDecimal price;
    /**
     * 商品排序
     */
    private Integer sort;
    /**
     * 是否有效；0无效、1有效
     */
    private Integer isEnabled;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

}
