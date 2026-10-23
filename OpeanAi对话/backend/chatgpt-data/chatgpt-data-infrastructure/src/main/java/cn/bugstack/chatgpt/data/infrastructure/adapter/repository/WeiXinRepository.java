package cn.bugstack.chatgpt.data.infrastructure.adapter.repository;

import cn.bugstack.chatgpt.data.domain.weixin.repository.IWeiXinRepository;
import cn.bugstack.chatgpt.data.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RTransaction;
import org.redisson.api.TransactionOptions;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 微信服务仓储
 * @create 2023-11-05 15:36
 */
@Slf4j
@Repository
public class WeiXinRepository implements IWeiXinRepository {

    private static final String Key = "weixin_code";

    @Resource
    private IRedisService redisService;

    @Override
    public String genCode(String openId) {

        String redisKey = Key + "_" + openId;

        // 分布式锁（防止并发重复）
        RLock lock = redisService.getLock(Key);
        try {
            // 设置锁超时时间 15 秒
            lock.lock(15, TimeUnit.SECONDS);

            // 获取值
            String isExitCode = redisService.getValue(redisKey);
            if (StringUtils.isNotBlank(isExitCode)) return isExitCode;

            String code = RandomStringUtils.randomNumeric(4);
            // 防重校验&重新生成
            for (int i = 0; i < 10 && StringUtils.isNotBlank(redisService.getValue(Key + "_" + code)); i++) {
                if (i < 3) {
                    code = RandomStringUtils.randomNumeric(4);
                } else if (i < 5) {
                    code = RandomStringUtils.randomNumeric(5);
                } else if (i < 9) {
                    code = RandomStringUtils.randomNumeric(6);
                    log.warn("验证码重复，生成6位字符串验证码 {} {}", openId, code);
                } else {
                    return "您的验证码获取失败，请重新回复405获取。";
                }
            }

            // 创建一个事务选项实例，设置事务超时等参数
            TransactionOptions options = TransactionOptions.defaults()
                    .timeout(3, TimeUnit.SECONDS);

            // 开始一个事务
            RTransaction transaction = redisService.createTransaction(options);
            try {
                // 存储值【3分钟有效期】
                redisService.setValue(Key + "_" + code, openId, 3 * 60 * 1000);
                redisService.setValue(Key + "_" + openId, code, 3 * 60 * 1000);
                // 提交事务
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback(); // 异常回滚
                }
                e.printStackTrace();
            }
            return code;
        } finally {
            lock.unlock();

        }
    }

}
