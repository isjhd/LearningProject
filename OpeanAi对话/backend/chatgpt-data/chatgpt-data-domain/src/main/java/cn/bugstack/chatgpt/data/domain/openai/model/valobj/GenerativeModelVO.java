package cn.bugstack.chatgpt.data.domain.openai.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 模型生成类型
 * @create 2023-11-25 14:09
 */
@Getter
@AllArgsConstructor
public enum GenerativeModelVO {

    TEXT("TEXT","文本"),
    IMAGES("IMAGES","图片"),
    ;

    private final String code;
    private final String info;

}
