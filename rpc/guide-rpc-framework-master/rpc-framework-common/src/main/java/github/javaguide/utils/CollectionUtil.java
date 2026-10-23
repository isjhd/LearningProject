package github.javaguide.utils;

import java.util.Collection;

/**
 * 集合工具类
 * 这是一个通用集合判空工具类，专门用于判断 Collection 系列集合（List/Set/Queue 等）是否为空
 *
 * @author zhanghua
 * @createTime 2022/3/6 12:58 上午
 */
public class CollectionUtil {

    /**
     * 判断集合是否为空
     * @param c 任意类型的集合（List/Set/Queue）
     * @return true=空集合，false=非空集合
     */
    public static boolean isEmpty(Collection<?> c) {
        // 核心判空逻辑：null 或 空集合 → 返回true
        return c == null || c.isEmpty();
    }

}
