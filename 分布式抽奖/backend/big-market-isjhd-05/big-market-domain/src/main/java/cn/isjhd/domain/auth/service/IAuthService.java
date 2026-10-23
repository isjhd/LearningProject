package cn.isjhd.domain.auth.service;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 认证
 * @create 2024-10-07 17:54
 */
public interface IAuthService {

    boolean checkToken(String token);

    String openid(String token);

}
