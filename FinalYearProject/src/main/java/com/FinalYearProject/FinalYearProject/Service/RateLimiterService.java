package com.FinalYearProject.FinalYearProject.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class RateLimiterService {
    private static final int quest_per_min =9;
    private final ProxyManager<String> proxyManager;

    public Bucket resolver(String key){
        Supplier<BucketConfiguration> configurationSupplier=this::getConfig;
        return proxyManager
                .builder()
                .build(key,configurationSupplier);
    }

    private BucketConfiguration getConfig() {
        return BucketConfiguration.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(
                                        quest_per_min
                                )
                                .refillIntervally(
                                        quest_per_min,
                                        Duration.ofMinutes(1)
                                )
                                .build()
                ).build();
    }
}
