package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.loadbalance.AbstractLoadBalance;
import github.javaguide.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * refer to dubbo consistent hash load balance:
 * https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 *
 * @author RicardoZ
 * @createTime 2020年10月20日 18:15:20
 * @description 一致性哈希负载均衡的具体实现
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    // 缓存：key=服务名，value=该服务的哈希选择器（线程安全容器）
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();


    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 1. 生成服务地址列表的「身份哈希」：区分「同一个服务的节点是否变化」
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // 2. 获取服务唯一名称，区分「不同服务」
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 3. 从缓存中获取该服务的哈希选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);

        // 4. 选择器为空 / 服务节点发生变化 → 重建哈希选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            // 一个哈希选择器（ConsistentHashSelector）只属于一个服务，不能跨服务共用！
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }

        // 5. 用「服务名+请求参数」生成key，选择服务节点
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector {
        // 哈希环：TreeMap存储「虚拟节点哈希 → 真实服务地址」
        // TreeMap自动排序，支持快速查找顺时针最近节点
        private final TreeMap<Long, String> virtualInvokers;
        // 服务节点的身份哈希（用于检测变更）
        private final int identityHashCode;

        // 构造器：初始化哈希环，创建虚拟节点
        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            // 遍历所有真实服务节点
            for (String invoker : invokers) {
                // 为每个真实节点创建 160 个虚拟节点（replicaNumber=160）
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 1. MD5计算摘要：保证哈希值均匀分布，固定长度（16 字节）
                    // 不加 i → 160 个虚拟节点挤在同一个点
                    byte[] digest = md5(invoker + i);
                    // 2. 1个MD5摘要生成4个虚拟节点哈希
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        // 3. 存入哈希环
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        // 将任意字符串（服务地址 / 请求参数）计算成固定长度（16 字节）的 MD5 摘要字节数组。
        // MD5 不是为了加密，而是为了把不规则的字符串转换成均匀、无规律的字节数组
        // 让哈希环上的虚拟节点分布更均匀，避免请求倾斜。
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                // 1. 获取MD5算法的消息摘要实例（Java标准加密算法）
                md = MessageDigest.getInstance("MD5");
                // 2. 将字符串转换为【UTF-8编码】的字节数组（统一编码，保证跨平台结果一致）
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                // 3. 将字节数组更新到摘要对象中（准备计算）
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            // 5. 计算并返回最终的MD5摘要（固定16字节长度）
            return md.digest();
        }

        //
        static long hash(byte[] digest, int idx) {
            // &255、& 4294967295L转成无符号整数
            // md5 生成的是纯字节数据（0~255）；所以全部需要生成无符号整数
            // Java 把它当成有符号 byte，会出现负数；
            // 用了负数，一致性哈希的【环形逻辑】会直接报废
            // 因为你的请求哈希已经是正数了，所以节点哈希必须也是正数，否则永远匹配不上！

            // byte/short 参与数学运算、位运算时，会自动转换成 int，且按照「符号位」填充高位
            //正数（符号位 0）→ 高位补 0
            //负数（符号位 1）→ 高位全部补 1
            //这个行为，会直接毁掉你的哈希拼接！
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey) {
            // 1. 对请求唯一标识做MD5摘要（保证散列均匀）
            byte[] digest = md5(rpcServiceKey);
            // 2. 生成32位哈希值，调用底层方法查找节点（只取第0段，足够定位）
            return selectForKey(hash(digest, 0));
        }

        public String selectForKey(long hashCode) {
            // 1. 核心：在哈希环上找 ≥ 当前哈希值的第一个节点（顺时针查找）
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();

            // 2. 边界处理：如果到了环的末尾，就回到环的开头（环形结构）
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            // 3. 返回找到的真实服务地址
            return entry.getValue();
        }
    }
}
