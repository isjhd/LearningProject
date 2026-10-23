package github.javaguide.extension;

// 持有实例的包装类，保证多线程下的可见性
// volatile 保证【变量本身】的可见性，不保证【变量指向的对象内部】的可见性！
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
