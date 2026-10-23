package github.javaguide.annotation;


import java.lang.annotation.*;

/**
 * 标记 RPC 服务引用字段，实现远程服务的自动注入（类似 Spring @Autowired）
 *
 * @author smile2coder
 * @createTime 2020年09月16日 21:42:00
 * @description 服务引用
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)// 运行时保留，支持反射解析。
@Target({ElementType.FIELD}) // 仅能标注在字段上；
@Inherited// 子类可继承父类的该注解；
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     * 区分同一服务的不同版本（如 v1、v2）；
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     * 对服务分组（如按业务模块、环境划分）。
     */
    String group() default "";

}
