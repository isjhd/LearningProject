package cn.isjhd.domain.award.model.aggregate;

import cn.isjhd.domain.award.model.entity.TaskEntity;
import cn.isjhd.domain.award.model.entity.UserAwardRecordEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: isjhd
 * @Date: 2025-12-09 14:40
 * @Description: 用户中奖记录聚合对象【聚合代表一个事务操作】
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAwardRecordAggregate {

    private UserAwardRecordEntity userAwardRecordEntity;
    private TaskEntity taskEntity;

}
