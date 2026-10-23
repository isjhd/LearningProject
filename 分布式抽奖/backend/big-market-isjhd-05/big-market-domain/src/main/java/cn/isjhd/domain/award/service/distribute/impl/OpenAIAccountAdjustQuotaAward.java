package cn.isjhd.domain.award.service.distribute.impl;

import cn.isjhd.domain.award.adapter.port.IAwardPort;
import cn.isjhd.domain.award.adapter.repository.IAwardRepository;
import cn.isjhd.domain.award.model.entity.DistributeAwardEntity;
import cn.isjhd.domain.award.service.IAwardService;
import cn.isjhd.domain.award.service.distribute.IDistributeAward;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAI 账户调额奖品
 * @create 2024-10-06 11:18
 */
@Component("openai_use_count")
public class OpenAIAccountAdjustQuotaAward implements IDistributeAward {

    @Resource
    private IAwardPort port;
    @Resource
    private IAwardRepository repository;

    @Override
    public void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) throws Exception {
        // 奖品ID
        Integer awardId = distributeAwardEntity.getAwardId();
        // 查询奖品ID 「优先走透传的随机积分奖品配置」
        String awardConfig = distributeAwardEntity.getAwardConfig();
        if (StringUtils.isBlank(awardConfig)) {
            awardConfig = repository.queryAwardConfig(awardId);
        }
        // 发奖接口
        port.adjustAmount(distributeAwardEntity.getUserId(), Integer.valueOf(awardConfig));
    }

}
