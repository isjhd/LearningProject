package github.javaguide.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 08:24:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {

    // serialVersionUID：序列化版本 UID，防止反序列化失败（Java 序列化机制要求）。
    private static final long serialVersionUID = 1905122041950251207L;
    // 全局唯一请求 ID
    private String requestId;
    // 调用的接口全类名，服务端靠它找实现类
    private String interfaceName;
    // 调用的方法名
    private String methodName;
    // 方法的实际参数值
    private Object[] parameters;
    // 方法的参数类型，解决方法重载问题
    private Class<?>[] paramTypes;
    // 服务版本号，区分同一个接口的多版本实现
    private String version;
    // 服务分组，用于环境隔离 / 灰度发布
    private String group;

    // 拼接生成 唯一的服务标识 Key。
    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
