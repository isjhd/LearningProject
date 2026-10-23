package github.javaguide.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo serialization class, Kryo serialization efficiency is very high, but only compatible with Java language
 * 基于 Kryo 高性能序列化框架的 Serializer 接口实现，是纯 Java 环境下序列化性能的天花板
 *
 * Kryo 框架特性
 * 1. 纯 Java 二进制序列化，性能远超 Hessian / JDK 原生序列化；
 * 2. 致命缺陷：不支持跨语言，只能用于 Java 服务之间通信；
 * 3. 非线程安全：Kryo 实例不能多线程共用，否则会数据错乱、抛并发异常。
 *
 * @author shuang.kou
 * @createTime 2020年05月13日 19:29:00
 */
@Slf4j
public class KryoSerializer implements Serializer {

    // 因为 Kryo 不是线程安全的，所以用 ThreadLocal 存储每个线程独立的 Kryo 对象
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 注册RPC核心实体类：性能优化！
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });


    @Override
    public byte[] serialize(Object obj) {
        // try-with-resources：自动关闭流，杜绝资源泄漏
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            // 获取当前线程的专属 Kryo 实例
            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            // 打印完整异常堆栈 + 抛自定义异常
            log.error("Serialization failed", e);
            throw new SerializeException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            log.error("Deserialization failed", e);
            throw new SerializeException("Deserialization failed", e);
        }
    }

    public class SerializeException extends RuntimeException {
        public SerializeException(String message) {
            super(message);
        }

        public SerializeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
