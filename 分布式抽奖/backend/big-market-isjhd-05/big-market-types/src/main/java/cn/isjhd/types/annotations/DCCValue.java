package cn.isjhd.types.annotations;

import java.lang.annotation.*;

/**
 * @Author: isjhd
 * @Date: 2025-12-30 10:36
 * @Description: 动态配置中心
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DCCValue {

    String value() default "";

}
