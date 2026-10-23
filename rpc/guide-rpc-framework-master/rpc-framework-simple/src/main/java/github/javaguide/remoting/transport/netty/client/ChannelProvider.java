package github.javaguide.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * store and get Channel object
 * 线程安全地缓存、获取、清理客户端与服务端的 Channel 连接
 * @author shuang.kou
 * @createTime 2020年05月29日 16:36:00
 */
@Slf4j
public class ChannelProvider {

    // 线程安全的Map：key=IP:端口字符串，value=Netty连接通道
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }


    public Channel get(InetSocketAddress inetSocketAddress) {
        // 1. 将网络地址转为字符串作为唯一key
        String key = inetSocketAddress.toString();
        // 2. 检查缓存中是否存在该地址的连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // 3. 校验连接：非空 + 处于活跃状态（TCP连接正常）
            if (channel != null && channel.isActive()) {
                return channel; // 直接复用有效连接
            } else {
                channelMap.remove(key); // 连接无效/已关闭，自动清理脏数据
            }
        }
        return null; // 无有效连接，返回null
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);// 覆盖式存储最新连接
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }
}
