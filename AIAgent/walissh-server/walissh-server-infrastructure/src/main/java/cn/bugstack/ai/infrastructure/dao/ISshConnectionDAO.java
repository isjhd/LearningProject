package cn.bugstack.ai.infrastructure.dao;

import cn.bugstack.ai.infrastructure.dao.po.SshConnectionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SSH连接配置DAO
 *
 * @author waissh dev
 */
@Mapper
public interface ISshConnectionDAO {

    void insert(SshConnectionPO po);

    void update(SshConnectionPO po);

    void delete(@Param("connectionId") String connectionId);

    SshConnectionPO queryByConnectionId(@Param("connectionId") String connectionId);

    List<SshConnectionPO> queryListByUserId(@Param("userId") String userId);

}
