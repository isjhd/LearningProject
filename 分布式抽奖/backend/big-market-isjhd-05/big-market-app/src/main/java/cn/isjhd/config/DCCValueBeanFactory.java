package cn.isjhd.config;

import cn.isjhd.types.annotations.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: isjhd
 * @Date: 2025-12-30 10:42
 * @Description: 基于 Zookeeper 的配置中心实现原理
 */
@Slf4j
public class DCCValueBeanFactory implements BeanPostProcessor {

    private static final String BASE_CONFIG_PATH = "/big-market-dcc";
    private static final String BASE_CONFIG_PATH_CONFIG = BASE_CONFIG_PATH + "/config";

    private CuratorFramework client;

    private final Map<String, Object> dccObjGroup = new HashMap<>();

    public DCCValueBeanFactory(CuratorFramework client) throws Exception {
        if (null == client) return;
        this.client = client;

        // 1. 检查并创建Zookeeper配置根节点（不存在则创建）
        if (null == client.checkExists().forPath(BASE_CONFIG_PATH_CONFIG)) {
            client.create().creatingParentsIfNeeded().forPath(BASE_CONFIG_PATH_CONFIG);
            log.info("DCC 节点监听 base node {} not absent create new done!", BASE_CONFIG_PATH_CONFIG);
        }

        // 2. 创建CuratorCache（Curator提供的轻量级节点监听工具），监听配置根节点下的所有子节点
        CuratorCache curatorCache = CuratorCache.build(client, BASE_CONFIG_PATH_CONFIG);
        curatorCache.start();

        // 3. 添加节点变更监听器：只处理NODE_CHANGED（节点值修改）事件
        curatorCache.listenable().addListener((type, oldData, data) -> {
            switch (type) {
                case NODE_CHANGED:
                    String dccValuePath = data.getPath(); // 变更的节点路径
                    Object objBean = dccObjGroup.get(dccValuePath); // 获取对应的Bean实例
                    if (null == objBean) return;
                    try {
                        // 处理AOP代理：如果Bean是代理对象，获取真实的目标类（否则读不到注解字段）
                        Class<?> objBeanClass = objBean.getClass();
                        // 检查 objBean 是否是代理对象
                        if (AopUtils.isAopProxy(objBean)) {
                            // 获取代理对象的目标对象
                            objBeanClass = AopUtils.getTargetClass(objBean);
//                            objBeanClass = AopProxyUtils.ultimateTargetClass(objBean);
                        }

                        // 从节点路径中提取字段名（如从/big-market-dcc/config/degradeSwitch提取degradeSwitch）
                        String fieldName = dccValuePath.substring(dccValuePath.lastIndexOf("/") + 1);
                        // getDeclaredField 方法用于获取指定类中声明的所有字段，包括私有字段、受保护字段和公共字段。
                        Field field = objBeanClass.getDeclaredField(fieldName);
                        field.setAccessible(true);// 突破私有字段访问限制
                        field.set(objBean, new String(data.getData()));// 把Zookeeper新的节点值注入到字段
                        field.setAccessible(false);// 恢复访问权限

                        log.info("DCC 节点监听 listener value set");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    // 在每个 Spring Bean 初始化完成后执行，扫描@DCCValue注解并注入配置：
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (client == null) return bean;

        // 注意；增加 AOP 代理后，获得类的方式要通过 AopProxyUtils.getTargetClass(bean); 不能直接 bean.class 因为代理后类的结构发生变化，这样不能获得到自己的自定义注解了。
        Class<?> targetBeanClass = bean.getClass();
        Object targetBeanObject = bean;
        if (AopUtils.isAopProxy(bean)) {
            targetBeanClass = AopUtils.getTargetClass(bean);
            targetBeanObject = AopProxyUtils.getSingletonTarget(bean);
        }

        // 扫描Bean的所有字段
        Field[] fields = targetBeanClass.getDeclaredFields();
        for (Field field : fields) {
            // 跳过没有@DCCValue注解的字段
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }

            // 获取注解实例
            DCCValue dccValue = field.getAnnotation(DCCValue.class);

            // @DCCValue("degradeSwitch:close")
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case 「isSwitch/isSwitch:1」");
            }

            String[] splits = value.split(":");
            String key = splits[0];
            String defaultValue = splits.length == 2 ? splits[1] : null;

            try {
                // 判断当前节点是否存在，不存在则创建出 Zookeeper 节点
                String keyPath = BASE_CONFIG_PATH_CONFIG.concat("/").concat(key);
                // 检查节点是否存在
                if (null == client.checkExists().forPath(keyPath)) {
                    // 节点不存在：创建节点 + 注入默认值（如果有）
                    client.create().creatingParentsIfNeeded().forPath(keyPath);
                    if (StringUtils.isNotBlank(defaultValue)) {
                        field.setAccessible(true);
                        field.set(targetBeanObject, defaultValue);
                        field.setAccessible(false);
                    }
                    log.info("DCC 节点监听 创建节点 {}", keyPath);
                } else {
                    // 节点存在：读取Zookeeper节点值并注入到字段
                    String configValue = new String(client.getData().forPath(keyPath));
                    if (StringUtils.isNotBlank(configValue)) {
                        field.setAccessible(true);
                        field.set(targetBeanObject, configValue);
                        field.setAccessible(false);
                        log.info("DCC 节点监听 设置配置 {} {} {}", keyPath, field.getName(), configValue);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 将节点路径和Bean实例存入缓存（用于后续节点变更时更新）
            dccObjGroup.put(BASE_CONFIG_PATH_CONFIG.concat("/").concat(key), targetBeanObject);

        }

        return bean;
    }

}
