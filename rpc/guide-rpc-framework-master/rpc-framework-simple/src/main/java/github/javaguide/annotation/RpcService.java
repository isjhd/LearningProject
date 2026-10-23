package github.javaguide.annotation;


import java.lang.annotation.*;

/**
 * 标记 RPC 服务实现类，声明该类为对外提供的 RPC 服务
 *
 * @author shuang.kou
 * @createTime 2020年07月21日 13:11:00
 * @description 服务发布
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，支持反射解析。
@Target({ElementType.TYPE})// 仅能标注在类 / 接口上；
@Inherited // 子类可继承父类的该注解；
public @interface RpcService {

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
