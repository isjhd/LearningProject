package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private Integer database;

    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useReplicatedServers()
                .setDatabase(database)
                .setPassword("".equals(password) ? null : password) // 这样没有密码就用默认值 null 有密码就用密码更加灵活
                .addNodeAddress(String.format("redis://%s:%s", host, port));
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
