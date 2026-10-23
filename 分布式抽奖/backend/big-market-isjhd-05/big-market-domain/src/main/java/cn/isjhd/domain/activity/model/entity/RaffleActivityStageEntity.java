package cn.isjhd.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动上架实体对象
 * @create 2024-10-26 18:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleActivityStageEntity {

    /** 自增ID */
    private Long id;
    /** 渠道 */
    private String channel;
    /** 来源 */
    private String source;
    /** 活动ID */
    private Long activityId;
    /** 上架状态；create、active、expire */
    private String state;

}
