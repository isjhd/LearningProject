package cn.isjhd.domain.activity.service.quota.rule;

/**
 * @description: 下单规则责任链抽象类
 * @author: isjhd
 * @date: 2025/12/2 15:12
 */
public abstract class AbstractActionChain implements IActionChain{

    private IActionChain next;

    @Override
    public IActionChain next() {
        return next;
    }

    @Override
    public IActionChain appendNext(IActionChain next) {
        this.next = next;
        return next;
    }

}
