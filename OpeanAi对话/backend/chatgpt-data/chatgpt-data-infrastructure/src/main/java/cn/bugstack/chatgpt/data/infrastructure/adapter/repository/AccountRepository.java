package cn.bugstack.chatgpt.data.infrastructure.adapter.repository;

import cn.bugstack.chatgpt.data.domain.account.adapter.repository.IAccountRepository;
import cn.bugstack.chatgpt.data.domain.account.model.entity.AdjustQuotaEntity;
import cn.bugstack.chatgpt.data.domain.account.model.valobj.AccountQuotaVO;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.UserAccountStatusVO;
import cn.bugstack.chatgpt.data.infrastructure.dao.IUserAccountDao;
import cn.bugstack.chatgpt.data.infrastructure.dao.po.UserAccountPO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;


/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户仓储服务
 * @create 2024-10-06 09:14
 */
@Repository
public class AccountRepository implements IAccountRepository {

    @Value("${app.config.model-types:glm-4,gpt-4,chatglm_pro}")
    private String modelTypes;

    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity) {

        // 查询额度
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(adjustQuotaEntity.getOpenid());
        if (null != userAccountPO){
            // 构建参数
            UserAccountPO userAccountPOReq = new UserAccountPO();
            userAccountPOReq.setOpenid(adjustQuotaEntity.getOpenid());
            userAccountPOReq.setTotalQuota(adjustQuotaEntity.getIncreaseQuota());
            userAccountPOReq.setSurplusQuota(adjustQuotaEntity.getIncreaseQuota());
            // 调整额度
            userAccountDao.addAccountQuota(userAccountPOReq);
        }else {
            // 构建参数
            UserAccountPO userAccountPOReq = new UserAccountPO();
            userAccountPOReq.setOpenid(adjustQuotaEntity.getOpenid());
            userAccountPOReq.setTotalQuota(adjustQuotaEntity.getIncreaseQuota());
            userAccountPOReq.setSurplusQuota(adjustQuotaEntity.getIncreaseQuota());
            userAccountPOReq.setStatus(UserAccountStatusVO.AVAILABLE.getCode());
            userAccountPOReq.setModelTypes(modelTypes);
            userAccountDao.insert(userAccountPOReq);
        }

        userAccountPO = userAccountDao.queryUserAccount(adjustQuotaEntity.getOpenid());

        // 返回结果
        return AccountQuotaVO.builder()
                .totalQuota(userAccountPO.getTotalQuota())
                .surplusQuota(userAccountPO.getSurplusQuota())
                .build();
    }

    @Override
    public AccountQuotaVO queryAccountQuota(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return AccountQuotaVO.builder().totalQuota(0).surplusQuota(0).build();
        return AccountQuotaVO.builder()
                .totalQuota(userAccountPO.getTotalQuota())
                .surplusQuota(userAccountPO.getSurplusQuota())
                .build();
    }

}
