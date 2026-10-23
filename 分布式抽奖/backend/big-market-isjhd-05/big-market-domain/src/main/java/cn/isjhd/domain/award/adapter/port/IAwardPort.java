package cn.isjhd.domain.award.adapter.port;

import java.io.IOException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 奖品对接接口
 * @create 2024-10-06 11:26
 */
public interface IAwardPort {

    void adjustAmount(String userId, Integer increaseQuota) throws Exception;

}
