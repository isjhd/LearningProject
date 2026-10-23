package github.javaguide.factory;

import github.javaguide.extension.Holder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 获取单例对象的工厂类
 *
 * @author shuang.kou
 * @createTime 2020年06月03日 15:04:00
 */
@Slf4j
public final class SingletonFactory {

    // 旧版缓存：ConcurrentHashMap（存在复合操作线程安全隐患）
    // 因为它只保证：get() 是安全的，put() 是安全的。但不保证「get + 逻辑 + put」连在一起是原子的！
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();
    // 新版缓存：HashMap + Holder（锁控制写操作，线程安全）
    private static final Object lock = new Object();
    // 私有锁对象：比锁 SingletonFactory.class 更安全（避免外部锁竞争）
    private static final Map<String, Holder<Object>> OBJECT_MAP_NEW = new HashMap<>();

    private SingletonFactory() {
    }

    /**
     * 无参构造创建单例（支持私有构造）
     * 通过类对象获取类实例
     */
    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        String key = c.getName();

        // 1. 第一次检查：快速读取缓存（无锁）
        Holder<Object> holder = OBJECT_MAP_NEW.get(key);
        if (holder != null && holder.get() != null) {
            // 1.1 holder保证了可见性，从而不会使用没有初始化的对象
            // 1.2 可以将对象强制转换为特定的类或接口类型，并在转换过程中进行类型检查，以避免在运行时出现类型不匹配的错误。
            return c.cast(holder.get());
        }

        // 2. 同步块：确保只有一个线程创建实例
        synchronized (lock) {
            // 3. 第二次检查：防止其他线程已创建holder
            holder = OBJECT_MAP_NEW.computeIfAbsent(key, k -> new Holder<>());

            // 4. 创建实例（此处不需要再次检查holder.get()，因为锁保证了互斥性）
            if (holder.get() == null) {
                try {
                    // 通过类的 Class 对象，获取该类的【无参构造方法】
                    Constructor<T> constructor =  c.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    // 通过反射机制可以动态地创建类的实例。
                    T instance = constructor.newInstance();
                    // 4.2 放入到map里面
                    holder.set(instance);
                } catch (Exception e) {
                    throw new RuntimeException("创建示例失败", e);
                }
            }
        }

        return c.cast(holder.get());
    }

    // 自定义构造器创建单例（带参/工厂方法）
    public static <T> T getInstance(Supplier<T> constructor, Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        String key = c.getName();

        // 1. 第一次检查：快速读取缓存（无锁）
        Holder<Object> holder = OBJECT_MAP_NEW.get(key);
        if (holder != null && holder.get() != null) {
            // 1.1 holder保证了可见性，从而不会使用没有初始化的对象
            return c.cast(holder.get());
        }

        // 2. 同步块：确保只有一个线程创建实例
        synchronized (lock) {
            // 3. 第二次检查：防止其他线程已创建holder
            holder = OBJECT_MAP_NEW.computeIfAbsent(key, k -> new Holder<>());

            // 4. 创建实例（此处不需要再次检查holder.get()，因为锁保证了互斥性）
            if (holder.get() == null) {
                try {
                    // 4.1 创建对象
                    T instance = constructor.get();
                    // 4.2 放入到map里面
                    holder.set(instance);
                } catch (Exception e) {
                    throw new RuntimeException("创建示例失败", e);
                }
            }
        }

        return c.cast(holder.get());
    }

    // 无参创建 + 自定义初始化
    public static <T> T getInstance(Consumer<T> initConsumer, Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        String key = c.getName();

        // 1. 第一次检查：快速读取缓存（无锁）
        Holder<Object> holder = OBJECT_MAP_NEW.get(key);
        if (holder != null && holder.get() != null) {
            // 1.1 holder保证了可见性，从而不会使用没有初始化的对象
            return c.cast(holder.get());
        }

        // 2. 同步块：确保只有一个线程创建实例
        synchronized (lock) {
            // 3. 第二次检查：防止其他线程已创建holder
            holder = OBJECT_MAP_NEW.computeIfAbsent(key, k -> new Holder<>());

            // 4. 创建实例（此处不需要再次检查holder.get()，因为锁保证了互斥性）
            if (holder.get() == null) {
                try {
                    // 4.1 创建对象
                    T instance = c.getDeclaredConstructor().newInstance();
                    // 4.2 初始化对象
                    initConsumer.accept(instance);
                    // 4.2 放入到map里面
                    holder.set(instance);
                } catch (Exception e) {
                    throw new RuntimeException("创建示例失败", e);
                }
            }
        }

        return c.cast(holder.get());
    }

    // 旧版方法（保留兼容）
    public static <T> T getInstanceOld(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return c.cast(OBJECT_MAP.get(key));
        } else {
            synchronized (lock) {
                if (!OBJECT_MAP.containsKey(key)) {
                    try {
                        T instance = c.getDeclaredConstructor().newInstance();
                        OBJECT_MAP.put(key, instance);
                        return instance;
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else {
                    return c.cast(OBJECT_MAP.get(key));
                }
            }
        }
    }
}
