package com.FinalYearProject.FinalYearProject.Config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

//    @Value("${spring.data.redis.host:localhost}")
//    private String host;
//
//    @Value("${spring.data.redis.port:6379}")
//    private int port;

    @Value("${spring.data.redis.url:redis://localhost:6379}")
    private String link;
    @Bean
    public RedisClient redisClient() {
        // Fix: Use the static 'create' method, not an instance call
        return RedisClient.create(
                RedisURI.create(link)
        );
    }

    @Bean
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        // 1. Create a connection specifically for String keys and byte[] values
        // Bucket stores the bucket state as a byte array (binary)
        StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        // 2. Define the expiration strategy (Cleanup inactive buckets after 1 day)
        // Note: renamed to 'fixedTimeToLive' in recent 8.x versions
        ExpirationAfterWriteStrategy expiration = ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofDays(1));

        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                .withExpirationAfterWriteStrategy(expiration);

        // 3. Build the manager using the specific connection
        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withClientSideConfig(clientSideConfig)
                .build();
    }
}