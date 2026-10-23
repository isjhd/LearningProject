package cn.isjhd.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: isjhd
 * @Date: 2025-12-17 11:46
 * @Description: 用户活动账户请求对象
 */
@Data
public class UserActivityAccountRequestDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}
