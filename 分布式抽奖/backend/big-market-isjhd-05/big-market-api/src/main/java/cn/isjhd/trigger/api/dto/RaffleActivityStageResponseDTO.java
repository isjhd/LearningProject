package cn.isjhd.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 上架活动对象
 * @create 2024-10-26 18:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleActivityStageResponseDTO {

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
