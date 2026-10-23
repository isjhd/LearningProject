package cn.bugstack.chatgpt.data.trigger.mq;

import cn.bugstack.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单支付成功监听
 * 1. 订单支付成功回调，最好是快速变更订单状态，避免超时重试次数上限后不能做业务。所以推送出MQ消息来做【发货】流程
 * 2. 因为ChatGPT项目，选择了轻量的技术栈，所以使用 Guava 的 EventBus 消息总线来模拟消息使用。如果你后续的场景较大，也可以替换为 RocketMQ，教程：https://bugstack.cn/md/road-map/rocketmq.html
 * @create 2023-10-05 17:50
 */
@Slf4j
@Component
public class OrderPaySuccessListener {

    @Resource(name = "aliOrder")
    private IOrderService orderService;

    @Subscribe
    public void handleEvent(String orderId) {
        try {
            log.info("支付完成，发货并记录，开始。订单：{}", orderId);
            CreateOrderAggregate createOrderAggregate = orderService.queryOrder(orderId);
            // 发货
            orderService.deliverGoods(orderId);
            // 返利 - 营销活动
            orderService.rebateGoods(createOrderAggregate.getOpenid(), orderId);
        } catch (Exception e) {
            log.error("支付完成，发货并记录，失败。订单：{}", orderId, e);
        }
    }


}
