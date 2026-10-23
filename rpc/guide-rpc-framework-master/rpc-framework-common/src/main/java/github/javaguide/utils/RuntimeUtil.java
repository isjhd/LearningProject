package github.javaguide.utils;

/**
 * @author hty
 * @createTime 2020年09月18日 15:43:00
 * 动态获取 CPU 核心数
 */
public class RuntimeUtil {
    /**
     * 获取CPU的核心数
     *
     * @return cpu的核心数
     */
    public static int cpus() {
        // 1. 获取JVM运行时单例对象
        // 2. 调用方法返回可用CPU逻辑核心数
        return Runtime.getRuntime().availableProcessors();
    }
}
