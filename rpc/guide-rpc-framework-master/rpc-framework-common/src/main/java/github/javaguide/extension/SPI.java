package github.javaguide.extension;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)// 注解保留到运行时，允许通过反射获取注解
@Target(ElementType.TYPE)// 限定注解只能标记在 接口 / 类 上，不能标记在方法、字段上
public @interface SPI {
}
