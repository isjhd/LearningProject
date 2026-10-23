package cn.isjhd.trigger.http;

import cn.isjhd.domain.activity.model.entity.RaffleActivityStageEntity;
import cn.isjhd.domain.activity.service.IRaffleActivityStageService;
import cn.isjhd.domain.activity.service.armory.IActivityArmory;
import cn.isjhd.querys.adapter.repository.IESUserRaffleOrderRepository;
import cn.isjhd.querys.model.valobj.ESUserRaffleOrderVO;
import cn.isjhd.trigger.api.IErpOperateService;
import cn.isjhd.trigger.api.dto.ESUserRaffleOrderResponseDTO;
import cn.isjhd.trigger.api.dto.RaffleActivityStageResponseDTO;
import cn.isjhd.trigger.api.dto.UpdateStageActivity2ActiveRequestDTO;
import cn.isjhd.trigger.api.response.Response;
import cn.isjhd.types.enums.ResponseCode;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description ERP 运营接口
 * @create 2024-09-21 12:25
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/erp/")
@DubboService(version = "1.0")
public class ErpOperateController implements IErpOperateService {

    @Resource
    private IESUserRaffleOrderRepository userRaffleOrderRepository;
    @Resource
    private IRaffleActivityStageService raffleActivityStageService;
    @Resource
    private IActivityArmory activityArmory;

    /**
     * 查询运营数据，用户抽奖单列表
     * curl --request GET --url 'http://localhost:8098/api/v1/raffle/erp/query_user_raffle_order'
     */
    @RequestMapping(value = "query_user_raffle_order", method = RequestMethod.GET)
    @Override
    public Response<List<ESUserRaffleOrderResponseDTO>> queryUserRaffleOrder() {
        try {
            log.info("查询运营数据，用户抽奖单列表");
            List<ESUserRaffleOrderVO> userRaffleOrderVOList = userRaffleOrderRepository.queryESUserRaffleOrderVOList();

            List<ESUserRaffleOrderResponseDTO> userRaffleOrderResponseDTOS = new ArrayList<>();
            for (ESUserRaffleOrderVO esUserRaffleOrderVO : userRaffleOrderVOList) {
                ESUserRaffleOrderResponseDTO esUserRaffleOrderResponseDTO = new ESUserRaffleOrderResponseDTO();
                esUserRaffleOrderResponseDTO.setUserId(esUserRaffleOrderVO.getUserId());
                esUserRaffleOrderResponseDTO.setActivityId(esUserRaffleOrderVO.getActivityId());
                esUserRaffleOrderResponseDTO.setActivityName(esUserRaffleOrderVO.getActivityName());
                esUserRaffleOrderResponseDTO.setStrategyId(esUserRaffleOrderVO.getStrategyId());
                esUserRaffleOrderResponseDTO.setOrderId(esUserRaffleOrderVO.getOrderId());
                esUserRaffleOrderResponseDTO.setOrderTime(esUserRaffleOrderVO.getOrderTime());
                esUserRaffleOrderResponseDTO.setOrderState(esUserRaffleOrderVO.getOrderState());
                esUserRaffleOrderResponseDTO.setCreateTime(esUserRaffleOrderVO.getCreateTime());
                esUserRaffleOrderResponseDTO.setUpdateTime(esUserRaffleOrderVO.getUpdateTime());
                userRaffleOrderResponseDTOS.add(esUserRaffleOrderResponseDTO);
            }

            return Response.<List<ESUserRaffleOrderResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userRaffleOrderResponseDTOS)
                    .build();
        } catch (Exception e) {
            log.error("查询运营数据，用户抽奖单列表", e);
            return Response.<List<ESUserRaffleOrderResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_stage_activity_2_active", method = RequestMethod.POST)
    @Override
    public Response<Boolean> updateStageActivity2Active(@RequestBody UpdateStageActivity2ActiveRequestDTO requestDTO) {
        try {
            Long id = requestDTO.getId();
            log.info("更新上架活动状态为生效开始 id:{}", id);
            Long activityId = raffleActivityStageService.queryStageActivity2ActiveById(id);
            boolean assembled = activityArmory.assembleActivitySkuByActivityId(activityId);
            log.info("更新上架活动状态为装配完成 activityId:{} assembled:{}", activityId, assembled);

            raffleActivityStageService.updateStageActivity2Active(id);
            log.info("更新上架活动状态为生效完成 activityId:{}", activityId);

            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("更新上架活动状态为生效开始，失败 id:{}", requestDTO.getId(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_raffle_activity_stage_list", method = RequestMethod.GET)
    @Override
    public Response<List<RaffleActivityStageResponseDTO>> queryRaffleActivityStageList() {
        try {
            List<RaffleActivityStageResponseDTO> raffleActivityStageResponseDTOS = new ArrayList<>();
            List<RaffleActivityStageEntity> raffleActivityStageEntities = raffleActivityStageService.queryStageActivityList();
            for (RaffleActivityStageEntity raffleActivityStage : raffleActivityStageEntities) {
                RaffleActivityStageResponseDTO raffleActivityStageResponseDTO = RaffleActivityStageResponseDTO.builder()
                        .id(raffleActivityStage.getId())
                        .channel(raffleActivityStage.getChannel())
                        .source(raffleActivityStage.getSource())
                        .activityId(raffleActivityStage.getActivityId())
                        .state(raffleActivityStage.getState())
                        .build();
                raffleActivityStageResponseDTOS.add(raffleActivityStageResponseDTO);
            }

            Response<List<RaffleActivityStageResponseDTO>> response = Response.<List<RaffleActivityStageResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(raffleActivityStageResponseDTOS)
                    .build();

            log.info("查询上架活动数据 {}", JSON.toJSONString(response));

            return response;
        } catch (Exception e) {
            log.error("查询上架活动数据失败", e);
            return Response.<List<RaffleActivityStageResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
