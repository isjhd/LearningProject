package cn.bugstack.chatgpt.data.domain.weixin.repository;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 微信服务仓储
 * @create 2023-11-05 15:34
 */
public interface IWeiXinRepository {

    String genCode(String openId);

}
