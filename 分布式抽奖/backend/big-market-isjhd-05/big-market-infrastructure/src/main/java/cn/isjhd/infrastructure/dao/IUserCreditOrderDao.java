package cn.isjhd.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.isjhd.infrastructure.dao.po.UserCreditOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: isjhd
 * @Date: 2025-12-24 11:20
 * @Description: 用户积分流水单 DAO
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserCreditOrderDao {

    void insert(UserCreditOrder userCreditOrderReq);

}
