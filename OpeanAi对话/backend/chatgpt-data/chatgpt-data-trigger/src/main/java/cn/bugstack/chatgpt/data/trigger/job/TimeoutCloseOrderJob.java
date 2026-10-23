package cn.bugstack.chatgpt.data.trigger.job;

import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 超时关单任务
 * @create 2023-10-05 19:59
 */
@Slf4j
@Component()
public class TimeoutCloseOrderJob {

    @Resource(name = "aliOrder")
    private IOrderService orderService;

//    @Autowired(required = false)
//    private NativePayService payService;
//    @Value("${wxpay.config.mchid}")
//    private String mchid;

    @Resource
    private AlipayClient alipayClient;

//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void exec() {
//        try {
//            if (null == payService) {
//                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
//                return;
//            }
//            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
//            if (orderIds.isEmpty()) {
//                log.info("定时任务，超时30分钟订单关闭，暂无超时未支付订单 orderIds is null");
//                return;
//            }
//            for (String orderId : orderIds) {
//                boolean status = orderService.changeOrderClose(orderId);
//                //微信关单；暂时不需要主动关闭
//                CloseOrderRequest request = new CloseOrderRequest();
//                request.setMchid(mchid);
//                request.setOutTradeNo(orderId);
//                payService.closeOrder(request);
//
//                log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
//            }
//        } catch (Exception e) {
//            log.error("定时任务，超时15分钟订单关闭失败", e);
//        }
//    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            if (null == alipayClient) {
                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
                return;
            }
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
            if (orderIds.isEmpty()) {
                log.info("定时任务，超时30分钟订单关闭，暂无超时未支付订单 orderIds is null");
                return;
            }
            for (String orderId : orderIds) {

                //支付宝关单
                AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
                AlipayTradeCloseModel model = new AlipayTradeCloseModel();
                model.setOutTradeNo(orderId);
                request.setBizModel(model);
                alipayClient.execute(request);

                // todo 如果订单关闭成功了怎么样，如果关闭失败了怎么样
                boolean status = orderService.changeOrderClose(orderId);

                log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
            }
        } catch (Exception e) {
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }
    }

}
