package cn.bugstack.chatgpt.data.domain.openai.repository;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAi 仓储接口
 * @create 2023-10-03 16:49
 */
public interface IOpenAiRepository {

    int subAccountQuota(String openai);

    UserAccountQuotaEntity queryUserAccount(String openid);

}
