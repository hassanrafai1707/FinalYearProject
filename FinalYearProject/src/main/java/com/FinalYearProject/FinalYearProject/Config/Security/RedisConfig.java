package com.FinalYearProject.FinalYearProject.Config.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 Redis Configuration for Spring Data Redis
 PURPOSE:
 Configures RedisTemplate bean for Redis operations with proper serialization.
 Ensures consistent string-based serialization for keys and values.
 CONFIGURATION DETAILS:
 CONNECTION: Injects RedisConnectionFactory (auto-configured by Spring Boot)
 TEMPLATE: Creates RedisTemplate for Redis operations (CRUD, transactions)
 SERIALIZATION: Uses StringRedisSerializer for both keys and values
 Keys: Serialized as UTF-8 strings (standard Redis key format)
 Values: Serialized as UTF-8 strings (JSON/text data)
 WHY STRING SERIALIZATION:
 Human-readable keys/values in Redis CLI
 Compatible with other Redis clients
 Avoids Java serialization issues (versioning, security)
 Works with Redis String data type (most common use case)
 USAGE:
 Autowire RedisTemplate<String, String> for Redis operations
 Supports: set, get, delete, increment, list/set operations with string values
 NOTES:
 Configure Redis host/port in application.properties
 For complex objects, consider JSON or custom serializers
 Thread-safe template for concurrent operations
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory){
        RedisTemplate redisTemplate= new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
