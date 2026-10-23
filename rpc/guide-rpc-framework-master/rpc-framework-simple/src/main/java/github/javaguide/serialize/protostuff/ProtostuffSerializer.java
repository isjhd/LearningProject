package github.javaguide.serialize.protostuff;

import github.javaguide.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author TangMinXuan
 * @createTime 2020年11月09日 20:13
 *
 * 基于 Protostuff 高性能序列化框架的 Serializer 接口实现。
 * Protostuff 是 Google Protobuf 的 Java 优化封装版，核心优势是保留 Protobuf 的高性能，
 * 同时抛弃繁琐的 .proto 协议文件，是纯 Java 环境下仅次于 Kryo 的顶级序列化方案。
 *
 * Protostuff 特性
 * 1. 直接创建目标对象，无类型转换风险；
 * 2. 纯 Java 语言，不跨语言
 * 3. 线程安全设计比 Kryo 更轻量
 *
 */
public class ProtostuffSerializer implements Serializer {

    /**
     * 使用 ThreadLocal 为每个线程分配独立的 LinkedBuffer 实例。
     * <p>
     * 由于 LinkedBuffer 在多线程环境下是非线程安全的，因此通过 ThreadLocal
     * 确保每个线程都有自己专属的缓冲区，避免并发访问导致的数据竞争或序列化异常
     * 线程管理的多个channel调用序列化算法是安全的
     */
    private static final ThreadLocal<LinkedBuffer> BUFFER =
            ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));


    @Override
    public byte[] serialize(Object obj) {
        // 1. 获取对象类型
        Class<?> clazz = obj.getClass();
        // 2. 动态生成/获取对象结构描述 Schema（替代 .proto 文件）
        Schema schema = RuntimeSchema.getSchema(clazz);
        // 3. 获取当前线程专属缓冲区
        LinkedBuffer buffer = BUFFER.get();
        try {
            // 4. 核心：对象 → Protostuff 二进制字节数组
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            // 关键：清空缓冲区，供当前线程下次复用（必须操作）
            buffer.clear();// 清空复用
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // 1. 获取目标类的 Schema
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        // 2. 创建空的目标对象
        T obj = schema.newMessage();
        // 3. 核心：字节数组数据 → 合并到对象中（反序列化）
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
