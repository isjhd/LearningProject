package cn.bugstack.chatgpt.data.domain.openai.service.channel.model;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 模型生成文字/图片接口服务
 * @create 2023-11-25 13:54
 */
public interface IGenerativeModelService {

    void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws IOException;

}
