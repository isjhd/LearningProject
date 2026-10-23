package cn.isjhd.domain.credit.service.adjust;

import cn.isjhd.domain.credit.event.CreditAdjustSuccessMessageEvent;
import cn.isjhd.domain.credit.model.aggregate.TradeAggregate;
import cn.isjhd.domain.credit.model.entity.CreditAccountEntity;
import cn.isjhd.domain.credit.model.entity.CreditOrderEntity;
import cn.isjhd.domain.credit.model.entity.TaskEntity;
import cn.isjhd.domain.credit.model.entity.TradeEntity;
import cn.isjhd.domain.credit.model.valobj.TradeTypeVO;
import cn.isjhd.domain.credit.repository.ICreditRepository;
import cn.isjhd.domain.credit.service.ICreditAdjustService;
import cn.isjhd.types.enums.ResponseCode;
import cn.isjhd.types.event.BaseEvent;
import cn.isjhd.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 12:13
 * @Description: 积分调额服务【正逆向，增减积分】
 */
@Slf4j
@Service
public class CreditAdjustService implements ICreditAdjustService {

    @Resource
    private ICreditRepository creditRepository;
    @Resource
    private CreditAdjustSuccessMessageEvent creditAdjustSuccessMessageEvent;

    @Override
    public String createOrder(TradeEntity tradeEntity) {
        log.info("创建账户积分额度订单开始 userId:{} tradeName:{} amount:{}", tradeEntity.getUserId(), tradeEntity.getTradeName(), tradeEntity.getAmount());
        // 0. 判断处理，逆向交易，扣减积分，需要查询账户是否存在以及积分额度是否充足
        if (TradeTypeVO.REVERSE.equals(tradeEntity.getTradeType())) {
            CreditAccountEntity creditAccountEntity = creditRepository.queryUserCreditAccount(tradeEntity.getUserId());
            if (null == creditAccountEntity || creditAccountEntity.getAdjustAmount().compareTo(tradeEntity.getAmount()) < 0) {
                throw new AppException(ResponseCode.USER_CREDIT_ACCOUNT_NO_AVAILABLE_AMOUNT.getCode(), ResponseCode.USER_CREDIT_ACCOUNT_NO_AVAILABLE_AMOUNT.getInfo());
            }
        }

        // 1. 创建账户积分实体
        CreditAccountEntity creditAccountEntity = TradeAggregate.createCreditAccountEntity(
                tradeEntity.getUserId(),
                tradeEntity.getAmount());

        // 2. 创建账户订单实体
        CreditOrderEntity creditOrderEntity = TradeAggregate.createCreditOrderEntity(
                tradeEntity.getUserId(),
                tradeEntity.getTradeName(),
                tradeEntity.getTradeType(),
                tradeEntity.getAmount(),
                tradeEntity.getOutBusinessNo());

        // 3. 构建消息任务对象
        CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = new CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage();
        creditAdjustSuccessMessage.setUserId(tradeEntity.getUserId());
        creditAdjustSuccessMessage.setOrderId(creditOrderEntity.getOrderId());
        creditAdjustSuccessMessage.setAmount(tradeEntity.getAmount());
        creditAdjustSuccessMessage.setOutBusinessNo(tradeEntity.getOutBusinessNo());
        BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> creditAdjustSuccessMessageEventMessage = creditAdjustSuccessMessageEvent.buildEventMessage(creditAdjustSuccessMessage);

        TaskEntity taskEntity = TradeAggregate.createTaskEntity(tradeEntity.getUserId(), creditAdjustSuccessMessageEvent.topic(), creditAdjustSuccessMessageEventMessage.getId(), creditAdjustSuccessMessageEventMessage);

        // 4. 构建交易聚合对象
        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userId(tradeEntity.getUserId())
                .creditAccountEntity(creditAccountEntity)
                .creditOrderEntity(creditOrderEntity)
                .taskEntity(taskEntity)
                .build();

        // 5. 保存积分交易订单
        creditRepository.saveUserCreditTradeOrder(tradeAggregate);
        log.info("创建账户积分额度订单完成 userId:{} orderId:{}", tradeEntity.getUserId(), creditOrderEntity.getOrderId());

        return creditOrderEntity.getOrderId();
    }

    @Override
    public CreditAccountEntity queryUserCreditAccount(String userId) {
        return creditRepository.queryUserCreditAccount(userId);
    }
}