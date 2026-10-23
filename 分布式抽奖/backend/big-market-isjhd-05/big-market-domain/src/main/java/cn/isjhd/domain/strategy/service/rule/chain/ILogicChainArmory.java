package cn.isjhd.domain.strategy.service.rule.chain;

/**
 * @description: 责任链装配
 * @author: isjhd
 * @date: 2025/11/19 14:28
 */
public interface ILogicChainArmory {

    ILogicChain next();

    ILogicChain appendNext(ILogicChain next);

}
