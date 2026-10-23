package github.javaguide.spring;

import github.javaguide.annotation.RpcScan;
import github.javaguide.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * scan and filter specified annotations
 * 负责驱动自定义扫描器工作、自动注册 Bean
 *
 * @author shuang.kou
 * @createTime 2020年08月10日 22:12:00
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    /**
     * ImportBeanDefinitionRegistrar：允许开发者在应用上下文启动时，动态地注册Bean定义
     * ResourceLoaderAware：注入资源加载器，让扫描器能正确读取 classpath 下的 class 文件，保证扫描正常工作。
     */

    // Spring 组件固定扫描包（硬编码的默认包）
    private static final String SPRING_BEAN_BASE_PACKAGE = "github.javaguide";
    // @RpcScan 注解的属性名：basePackage
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    // Spring 资源加载器（由 ResourceLoaderAware 注入）
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        // 作用：让自定义扫描器能正确读取类路径、Jar 包中的 class 文件
        // 如果不注入，扫描器无法加载资源，会扫描不到任何类
        this.resourceLoader = resourceLoader;
    }

    /**
     *
     * @param annotationMetadata 提供了当前处理注解类的元数据信息，如注解属性值和类名
     * @param beanDefinitionRegistry 允许将新的Bean定义注册到Spring容器中。
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 获取 @RpcScan 注解的所有属性
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));

        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            // 获取注解中 basePackage 属性的值（用户配置的扫描包）
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }

        // 如果用户没配置 basePackage，默认扫描：当前@RpcScan标注类所在的包
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }

        // beanDefinitionRegistry：Bean 注册器（把扫描到的类注册为 Bean）
        // 注解 class：指定扫描目标注解
        // 扫描器1：专门扫描 @RpcService 注解（RPC 服务类）
        CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RpcService.class);
        // 扫描器2：专门扫描 @Component 注解（Spring 原生组件）
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);

        // 给扫描器设置资源加载器（保证扫描正常）。必须设置ResourceLoader，否则扫描不到类文件
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }

        // 扫描固定包下的 @Component 类
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);

        // 扫描 @RpcScan 指定包下的 @RpcService 类
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);

    }

}
