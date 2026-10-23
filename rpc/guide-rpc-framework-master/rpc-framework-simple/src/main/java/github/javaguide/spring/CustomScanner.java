package github.javaguide.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * custom package scanner
 * 扫描指定包下带有「自定义注解」的类，并自动将其注册为 Spring Bean
 *
 * @author shuang.kou
 * @createTime 2020年08月10日 21:42:00
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {// 作用：扫描 classpath、读取 .class 文件、解析类、生成 BeanDefinition、注册到容器

    /**
     *
     * @param registry Spring 容器的Bean 定义注册中心，负责存储和注册所有 Bean 的定义。
     * @param annoType 要扫描的目标注解类型（比如自定义的 @MyComponent、@RpcService 等）。
     */
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        // 1. 调用父类构造器，初始化 Spring 原生扫描器
        super(registry);
        // AnnotationTypeFilter：Spring 提供的注解类型过滤器，仅匹配带有指定注解的类。
        // addIncludeFilter：添加包含过滤器 → 只有匹配该过滤器的类，才会被扫描并注册为 Bean。
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    /**
     *
     * @param basePackages 要扫描的基础包路径（比如 com.example.service）
     * @return 扫描并成功注册的 Bean 数量。
     */
    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }
}