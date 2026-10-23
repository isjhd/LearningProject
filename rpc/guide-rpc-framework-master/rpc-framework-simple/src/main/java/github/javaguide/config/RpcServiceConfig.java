package github.javaguide.config;

import lombok.*;

/**
 * @author shuang.kou
 * @createTime 2020年07月21日 20:23:00
 * @description 封装服务实例、版本、分组，生成唯一的服务标识名称
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * 服务版本：区分同一个服务接口的不同版本（v1/v2）
     */
    private String version = "";
    /**
     * 服务分组：区分同一个接口的多个实现类（订单组/支付组）
     */
    private String group = "";

    /**
     * 目标服务对象：实际的业务服务实现类实例
     */
    private Object service;

    /**
     * 生成【唯一的RPC服务名称】
     * 规则：接口全限定名 + 分组 + 版本
     * 这个名称会注册到 ZooKeeper，作为服务的唯一标识
     */
    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /**
     * 获取服务实现类【实现的接口全限定名】
     * RPC 是面向接口编程，客户端只依赖接口，不依赖实现类
     */
    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
