package cn.isjhd.trigger.api;

import cn.isjhd.trigger.api.dto.ESUserRaffleOrderResponseDTO;
import cn.isjhd.trigger.api.dto.RaffleActivityStageResponseDTO;
import cn.isjhd.trigger.api.dto.UpdateStageActivity2ActiveRequestDTO;
import cn.isjhd.trigger.api.response.Response;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description ERP 运营接口
 * @create 2024-09-21 12:26
 */
public interface IErpOperateService {

    Response<List<ESUserRaffleOrderResponseDTO>> queryUserRaffleOrder();

    /**
     * 上架活动，上架后驱动装配
     *
     * @param requestDTO 上架流水ID
     */
    Response<Boolean> updateStageActivity2Active(UpdateStageActivity2ActiveRequestDTO requestDTO);

    Response<List<RaffleActivityStageResponseDTO>> queryRaffleActivityStageList();

}
