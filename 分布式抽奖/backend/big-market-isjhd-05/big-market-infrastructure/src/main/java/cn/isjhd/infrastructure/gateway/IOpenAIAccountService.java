package cn.isjhd.infrastructure.gateway;

import cn.isjhd.infrastructure.gateway.dto.AdjustQuotaRequestDTO;
import cn.isjhd.infrastructure.gateway.dto.AdjustQuotaResponseDTO;
import cn.isjhd.infrastructure.gateway.response.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAI应用项目账户服务接口
 * @create 2024-10-06 11:30
 */
public interface IOpenAIAccountService {

    @POST("/api/v1/account/adjust_quota")
    Call<Response<AdjustQuotaResponseDTO>> adjustQuota(@Body AdjustQuotaRequestDTO requestDTO);

}
