package github.javaguide.serialize.hessian;


import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import github.javaguide.exception.SerializeException;
import github.javaguide.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Hessian is a dynamically-typed, binary serialization and Web Services protocol designed for object-oriented transmission.
 * 基于 Hessian 二进制序列化协议的 Serializer 接口实现类
 *
 * Hessian特性
 * 1. 跨语言支持
 * 2. 线程安全
 *
 * @author Vinlee Xiao
 * @createTime 2022/2/23 21:11
 */
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        // try-with-resources：自动关闭流，无需手动释放资源（JDK7+特性）
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // 1. 创建 Hessian 输出对象，绑定字节输出流
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            // 2. 核心：将 Java 对象序列化为 Hessian 二进制格式
            hessianOutput.writeObject(obj);
            // 3. 转换为字节数组返回
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            // 1. 创建 Hessian 输入对象，绑定字节输入流
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            // 2. 核心：从二进制数据中还原出 Java 对象
            Object o = hessianInput.readObject();
            // 3. 安全的泛型类型转换（避免强制类型转换异常）
            return clazz.cast(o);

        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }

    }
}
