package cn.isjhd.domain.strategy.service.rule.tree;

import cn.isjhd.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

import java.util.Date;

// 规则树接口
public interface ILogicTreeNode {

    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue, Date endDateTime);

}
