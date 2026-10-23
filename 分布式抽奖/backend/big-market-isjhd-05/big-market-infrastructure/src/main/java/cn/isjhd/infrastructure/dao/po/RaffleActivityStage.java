package cn.isjhd.infrastructure.dao.po;

import lombok.Data;

import java.sql.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动展台 - 上架活动
 * @create 2024-10-26 17:06
 */
@Data
public class RaffleActivityStage {

    /** ID */
    private Long id;
    /** 渠道 */
    private String channel;
    /** 来源 */
    private String source;
    /** 活动ID */
    private Long activityId;
    /** 上架状态；create、active、expire */
    private String state;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
