package cn.bugstack.ai.infrastructure.dao;

import cn.bugstack.ai.infrastructure.dao.po.SshConnectionConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * SSH连接高级配置DAO
 *
 * @author waissh dev
 */
@Mapper
public interface ISshConnectionConfigDAO {

    void insertOrUpdate(SshConnectionConfigPO po);

    SshConnectionConfigPO queryByConnectionId(String connectionId);

}
