package cn.bugstack.chatgpt.data.domain.order.adapter.port;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 适配端口
 * @create 2024-10-20 14:51
 */
public interface IOrderPort {

    void rebate(String userId, String orderId);

}
