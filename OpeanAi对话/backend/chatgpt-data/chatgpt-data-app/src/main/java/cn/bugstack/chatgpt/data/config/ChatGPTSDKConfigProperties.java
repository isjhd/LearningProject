package cn.bugstack.chatgpt.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 20:36
 */
@Data
@ConfigurationProperties(prefix = "chatgpt.sdk.config", ignoreInvalidFields = true)
public class ChatGPTSDKConfigProperties {

    /** 状态；open = 开启、close 关闭 */
    private boolean enable;
    /** 转发地址 <a href="https://api.xfg.im/b8b6/">https://api.xfg.im/b8b6/</a> */
    private String apiHost;
    /** 可以申请 sk-*** */
    private String apiKey;
    /** 获取Token 仅为验证auth课程使用，后续已废弃 */
    @Deprecated
    private String authToken;

}
