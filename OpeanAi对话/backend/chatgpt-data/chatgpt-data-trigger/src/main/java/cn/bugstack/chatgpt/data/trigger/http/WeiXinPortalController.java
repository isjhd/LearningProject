package cn.bugstack.chatgpt.data.trigger.http;

import cn.bugstack.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import cn.bugstack.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import cn.bugstack.chatgpt.data.domain.weixin.service.IWeiXinBehaviorService;
import cn.bugstack.chatgpt.data.domain.weixin.service.IWeiXinValidateService;
import cn.bugstack.chatgpt.data.types.sdk.weixin.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 微信公众号，验签和请求应答
 * @create 2023-08-05 16:50
 */
@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/wx/portal/{appid}")
public class WeiXinPortalController {

    @Value("${wx.config.originalid}")
    private String originalId;

    @Resource
    private IWeiXinValidateService weiXinValidateService;

    @Resource
    private IWeiXinBehaviorService weiXinBehaviorService;

    /**
     * 处理微信服务器发来的get请求，进行签名的验证【apix.natapp1.cc 是我在 <a href="https://natapp.cn/">https://natapp.cn</a> 购买的渠道，你需要自己购买一个使用】
     * <a href="http://apix.natapp1.cc/api/v1/wx/portal/wxad979c0307864a66">http://apix.natapp1.cc/api/v1/wx/portal/wxad979c0307864a66</a>
     * <p>
     * appid     从 URL 路径中获取的微信AppID
     * signature 微信服务器生成的签名（用于验证请求合法性）
     * timestamp 微信服务器发送请求的时间戳
     * nonce     微信服务器生成的随机字符串（防止请求伪造）
     * echostr   微信服务器生成的验证回显字符串（验签通过后需原样返回）
     */
    @GetMapping(produces = "text/plain;charset=utf-8")// 指定接口返回纯文本格式且编码为 UTF-8，符合微信服务器对返回值的格式要求
    public String validate(@PathVariable String appid,
                           @RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息{}开始 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);

            // 非空校验：如果关键参数为空，抛出非法参数异常
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }


            boolean check = weiXinValidateService.checkSign(signature, timestamp, nonce);
            log.info("微信公众号验签信息{}完成 check：{}", appid, check);

            // 验签结果处理：通过则返回echostr，失败则返回null
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息{}失败 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    /**
     *
     * @param appid 从 URL 路径中获取公众号的唯一标识 appid
     * @param requestBody 接收微信推送的核心数据（XML 格式的消息体）
     * @param signature 微信服务器生成的签名 （用于验证请求合法性）
     * @param timestamp 微信服务器发送请求的时间戳
     * @param nonce 微信服务器生成的随机字符串（防止请求伪造）
     * @param openid 用户的微信唯一标识
     * @param encType 用于加密模式下的消息验证
     * @param msgSignature 用于加密模式下的消息验证
     * @return
     */
    @PostMapping(produces = "application/xml; charset=UTF-8")// @PostMapping：标识这是处理 POST 请求的接口，produces 指定返回值为 UTF-8 编码的 XML 格式
    public String post(@PathVariable String appid,
                       @RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {
            log.info("接收微信公众号信息请求{}开始 {}", openid, requestBody);
            // 消息转换
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);

            // 构建实体
            UserBehaviorMessageEntity entity = UserBehaviorMessageEntity.builder()
                    .openId(openid)
                    .fromUserName(message.getFromUserName())// 消息发送方（用户）标识
                    .msgType(message.getMsgType())// 消息类型（如text=文本、event=事件）
                    .content(StringUtils.isBlank(message.getContent()) ? null : message.getContent().trim())// 消息内容
                    .event(message.getEvent())// 事件类型（如subscribe=关注、unsubscribe=取消关注）
                    .createTime(new Date(Long.parseLong(message.getCreateTime()) * 1000L))// 时间戳转换（微信返回秒级，转Java毫秒级Date）
                    .build();

            // 受理消息
            String result = weiXinBehaviorService.acceptUserBehavior(entity);
            log.info("接收微信公众号信息请求{}完成 {}", openid, result);
            return result;
        } catch (Exception e) {
            log.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
            return "";
        }
    }

}
