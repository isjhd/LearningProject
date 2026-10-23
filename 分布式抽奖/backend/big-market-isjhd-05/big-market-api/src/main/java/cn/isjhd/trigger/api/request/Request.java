package cn.isjhd.trigger.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request<T> implements Serializable {

    /** 请求应用ID */
    private String appId;
    /** 请求应用Token */
    private String appToken;
    /** 请求对象 */
    private T data;

}
