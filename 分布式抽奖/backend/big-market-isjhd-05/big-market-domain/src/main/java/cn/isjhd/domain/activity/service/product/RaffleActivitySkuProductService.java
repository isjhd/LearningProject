package cn.isjhd.domain.activity.service.product;

import cn.isjhd.domain.activity.model.entity.SkuProductEntity;
import cn.isjhd.domain.activity.adapter.repository.IActivityRepository;
import cn.isjhd.domain.activity.service.IRaffleActivitySkuProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: isjhd
 * @Date: 2025-12-26 11:27
 * @Description: sku商品服务
 */
@Service
public class RaffleActivitySkuProductService implements IRaffleActivitySkuProductService {

    @Resource
    private IActivityRepository repository;

    @Override
    public List<SkuProductEntity> querySkuProductEntityListByActivityId(Long activityId) {
        return repository.querySkuProductEntityListByActivityId(activityId);
    }

}
