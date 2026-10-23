package github.javaguide.annotation;

import github.javaguide.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 自定义包扫描注解，替代 / 扩展 Spring 默认扫描逻辑，
 * 指定 RPC 框架需要扫描的包路径（框架会扫描这些包下带@RpcService的类并发布服务）。
 *
 * @author shuang.kou
 * @createTime 2020年08月10日 21:42:00
 * @description 注解扫描
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)// 导入自定义的扫描注册器，实现 Spring 扩展；
@Documented
public @interface RpcScan {

    String[] basePackage();// 扫描的基础包路径

}
