package cn.isjhd.trigger.rpc;

import cn.isjhd.domain.rebate.model.entity.BehaviorEntity;
import cn.isjhd.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.isjhd.domain.rebate.service.IBehaviorRebateService;
import cn.isjhd.trigger.api.IRebateService;
import cn.isjhd.trigger.api.dto.RebateRequestDTO;
import cn.isjhd.trigger.api.request.Request;
import cn.isjhd.trigger.api.response.Response;
import cn.isjhd.types.enums.ResponseCode;
import cn.isjhd.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 返利服务
 * @create 2024-10-20 13:44
 */
@Slf4j
@DubboService(version = "1.0")
public class RebateServiceRPC implements IRebateService {

    @Resource
    private Map<String, String> appTokenMap;

    @Resource
    private IBehaviorRebateService behaviorRebateService;

    @Override
    public Response<Boolean> rebate(Request<RebateRequestDTO> request) {
        RebateRequestDTO requestDTO = request.getData();
        log.info("返利操作开始 userId:{} request:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO));
        try {
            // 0. 参数校验
            if (StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getBehaviorType()) || StringUtils.isBlank(requestDTO.getOutBusinessNo()) || StringUtils.isBlank(request.getAppId()) || StringUtils.isBlank(request.getAppToken())) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 1. appid、apptoken 校验，给调用接口的一方，配置请求参数。
            if (!request.getAppToken().equals(appTokenMap.get(request.getAppId()))) {
                throw new AppException(ResponseCode.APP_TOKEN_ERROR.getCode(), ResponseCode.APP_TOKEN_ERROR.getInfo());
            }

            BehaviorEntity behaviorEntity = new BehaviorEntity();
            behaviorEntity.setUserId(requestDTO.getUserId());
            behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.valueOf(requestDTO.getBehaviorType().toUpperCase()));
            behaviorEntity.setOutBusinessNo(requestDTO.getOutBusinessNo());
            List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);

            log.info("返利操作完成 userId:{} orderIds:{}", requestDTO.getUserId(), JSON.toJSONString(orderIds));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();

        } catch (AppException e) {
            log.error("返利操作异常 userId:{} ", requestDTO.getUserId(), e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("返利操作失败 userId:{}", requestDTO.getUserId(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

}
