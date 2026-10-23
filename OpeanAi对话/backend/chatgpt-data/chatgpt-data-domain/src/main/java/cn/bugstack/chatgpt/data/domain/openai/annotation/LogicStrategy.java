package cn.bugstack.chatgpt.data.domain.openai.annotation;

import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // 仅作用于类/接口/枚举
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，允许反射获取
public @interface LogicStrategy {

    // 绑定规则枚举（核心属性）
    DefaultLogicFactory.LogicModel logicMode();

}
