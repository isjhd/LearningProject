package cn.bugstack.chatgpt.data.domain.account.service.quota;

import cn.bugstack.chatgpt.data.domain.account.adapter.repository.IAccountRepository;
import cn.bugstack.chatgpt.data.domain.account.model.entity.AdjustQuotaEntity;
import cn.bugstack.chatgpt.data.domain.account.model.valobj.AccountQuotaVO;
import cn.bugstack.chatgpt.data.domain.account.service.IAccountAdjustQuotaService;
import org.springframework.stereotype.Service;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户调额服务
 * @create 2024-10-06 08:54
 */
@Service
public class AccountAdjustQuotaService implements IAccountAdjustQuotaService {

    private final IAccountRepository repository;

    public AccountAdjustQuotaService(IAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity) {
        return repository.adjustQuota(adjustQuotaEntity);
    }

}
