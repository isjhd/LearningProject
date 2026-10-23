package cn.bugstack.chatgpt.data.infrastructure.adapter.repository;

import cn.bugstack.chatgpt.data.domain.auth.repository.IAuthRepository;
import cn.bugstack.chatgpt.data.infrastructure.redis.IRedisService;
import org.redisson.api.RTransaction;
import org.redisson.api.TransactionOptions;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 认证仓储服务
 * @create 2023-11-05 15:53
 */
@Repository
public class AuthRepository implements IAuthRepository {

    private static final String Key = "weixin_code";

    @Resource
    private IRedisService redisService;

    @Override
    public String getCodeUserOpenId(String code) {
        return redisService.getValue(Key + "_" + code);
    }

    @Override
    public void removeCodeByOpenId(String code, String openId) {
        // 创建一个事务选项实例，设置事务超时等参数
        TransactionOptions options = TransactionOptions.defaults()
                .timeout(3, TimeUnit.SECONDS);
        // 开始一个事务
        RTransaction transaction = redisService.createTransaction(options);
        try {
            redisService.remove(Key + "_" + code);
            redisService.remove(Key + "_" + openId);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

}
