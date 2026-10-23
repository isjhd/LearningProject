package cn.bugstack.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 模型对象
 * @create 2023-07-22 21:00
 */
@Getter
@AllArgsConstructor
public enum ChatGPTModel {

    /** gpt-3.5-turbo */
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GLM_4("glm-4"),
    /** 文生图 */
    DALL_E_2("dall-e-2"),
    DALL_E_3("dall-e-3"),

    ;
    private final String code;

}
