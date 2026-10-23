package cn.bugstack.chatgpt.data.trigger.job;

import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.common.eventbus.EventBus;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 检测未接收到或未正确处理的支付回调通知
 * @create 2023-10-05 19:52
 */
@Slf4j
@Component()
public class NoPayNotifyOrderJob {

    @Resource(name = "aliOrder")
    private IOrderService orderService;

//    @Autowired(required = false)
//    private NativePayService payService;
//    @Value("${wxpay.config.mchid}")
//    private String mchid;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private EventBus eventBus;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 微信支付
//    @Timed(value = "no_pay_notify_order_job", description = "定时任务，订单支付状态更新")
//    @Scheduled(cron = "0/3 * * * * ?")
//    public void exec() {
//        try {
//            if (null == payService) {
//                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
//                return;
//            }
//            List<String> orderIds = orderService.queryNoPayNotifyOrder();
//            if (orderIds.isEmpty()) {
//                log.info("定时任务，订单支付状态更新，暂无未更新订单 orderId is null");
//                return;
//            }
//            for (String orderId : orderIds) {
//                // 查询结果
//                QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
//                request.setMchid(mchid);
//                request.setOutTradeNo(orderId);
//                Transaction transaction = payService.queryOrderByOutTradeNo(request);
//                if (!Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
//                    log.info("定时任务，订单支付状态更新，当前订单未支付 orderId is {}", orderId);
//                    continue;
//                }
//                // 支付单号
//                String transactionId = transaction.getTransactionId();
//                Integer total = transaction.getAmount().getTotal();
//                BigDecimal totalAmount = new BigDecimal(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
//                String successTime = transaction.getSuccessTime();
//                // 更新订单
//                boolean isSuccess = orderService.changeOrderPaySuccess(orderId, transactionId, totalAmount, dateFormat.parse(successTime));
//                if (isSuccess) {
//                    // 发布消息
//                    eventBus.post(orderId);
//                }
//            }
//        } catch (Exception e) {
//            log.error("定时任务，订单支付状态更新失败", e);
//        }
//    }

    // 支付宝支付
    @Timed(value = "no_pay_notify_order_job", description = "定时任务，订单支付状态更新")
    @Scheduled(cron = "0/3 * * * * ?")
    public void exec() {
        try {
            if (null == alipayClient) {
                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
                return;
            }
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if (orderIds.isEmpty()) {
                log.info("定时任务，订单支付状态更新，暂无未更新订单 orderId is null");
                return;
            }
            for (String orderId : orderIds) {

                // 查询结果
                // 构造请求参数以调用接口
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                model.setOutTradeNo(orderId);
                request.setBizModel(model);

                AlipayTradeQueryResponse response = alipayClient.execute(request);

                if(!response.isSuccess() || !response.getTradeStatus().equals("TRADE_SUCCESS")) {
                    continue;
                }

                // 支付单号
                String transactionId = response.getTradeNo();
                String total = response.getBuyerPayAmount();
                BigDecimal totalAmount = new BigDecimal(total);
                Date successTime = response.getSendPayDate();

                // 更新订单
                boolean isSuccess = orderService.changeOrderPaySuccess(orderId, transactionId, totalAmount, successTime);
                if (isSuccess) {
                    // 发布消息
                    eventBus.post(orderId);
                }
            }
        } catch (AlipayApiException e) {
            log.info("定时任务，订单支付状态更新，当前订单未支付 orderId is {}", e);
        } catch (Exception e) {
            log.error("定时任务，订单支付状态更新失败", e);
        }
    }

}
