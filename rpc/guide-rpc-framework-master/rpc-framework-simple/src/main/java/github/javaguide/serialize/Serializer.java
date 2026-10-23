package github.javaguide.serialize;

import github.javaguide.extension.SPI;

/**
 * 序列化接口，所有序列化类都要实现这个接口
 *
 * @author shuang.kou
 * @createTime 2020年05月13日 19:29:00
 */
@SPI
public interface Serializer {
    /**
     * 序列化
     * 将 Java 对象 转换为 二进制字节数组；
     *
     * @param obj 任意 Java 对象（Object 兼容所有类型）
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * 将 二进制字节数组 还原为 指定类型的 Java 对象
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>   类的类型。举个例子,  {@code String.class} 的类型是 {@code Class<String>}.
     *              如果不知道类的类型的话，使用 {@code Class<?>}
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
