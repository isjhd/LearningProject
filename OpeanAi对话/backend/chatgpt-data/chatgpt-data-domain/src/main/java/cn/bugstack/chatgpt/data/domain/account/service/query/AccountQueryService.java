package cn.bugstack.chatgpt.data.domain.account.service.query;

import cn.bugstack.chatgpt.data.domain.account.adapter.repository.IAccountRepository;
import cn.bugstack.chatgpt.data.domain.account.model.valobj.AccountQuotaVO;
import cn.bugstack.chatgpt.data.domain.account.service.IAccountQueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 额度查询
 * @create 2024-10-19 09:21
 */
@Service
public class AccountQueryService implements IAccountQueryService {

    @Resource
    private IAccountRepository repository;

    @Override
    public AccountQuotaVO queryAccountQuota(String openid) {
        return repository.queryAccountQuota(openid);
    }

}
