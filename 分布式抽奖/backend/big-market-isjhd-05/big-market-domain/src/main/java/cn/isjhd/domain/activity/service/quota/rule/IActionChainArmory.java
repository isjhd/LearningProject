package cn.isjhd.domain.activity.service.quota.rule;

/**
 * @description: 抽奖动作责任链装配
 * @author: isjhd
 * @date: 2025/12/2 15:09
 */
public interface IActionChainArmory {

    IActionChain next();

    IActionChain appendNext(IActionChain next);

}
