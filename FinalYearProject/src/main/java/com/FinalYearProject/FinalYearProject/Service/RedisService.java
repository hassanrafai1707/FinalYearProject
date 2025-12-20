package com.FinalYearProject.FinalYearProject.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
/**
 * RedisService - Caching Service Abstraction Layer
 * PURPOSE: Service abstraction for Redis operations providing JSON serialization/deserialization, TTL management, and type-safe caching operations.
 * CACHING STRATEGY: Generic caching service that can cache any Java object as JSON in Redis with configurable Time-To-Live (TTL).
 * SERIALIZATION: Uses Jackson ObjectMapper for JSON serialization/deserialization. Supports complex object graphs and maintains type safety.
 * TTL MANAGEMENT: Configurable expiration time in minutes. Prevents cache staleness by automatically expiring entries after specified duration.
 * OPERATIONS: get(key, class) retrieves and deserializes object. set(key, object, ttl) serializes and stores with expiration. delete(key) removes entry.
 * ERROR HANDLING: Wraps Jackson exceptions in RuntimeException for cleaner service layer. Returns null for cache misses (no exception).
 * PERFORMANCE: RedisTemplate provides connection pooling and efficient serialization. JSON format enables cache inspection and debugging.
 * USE CASES: User session data, frequently accessed questions, generated paper templates, system configuration, or any expensive-to-compute data.
 * INTEGRATION: Works with RedisTemplate<String, String> configured in RedisConfig. Compatible with Spring Boot auto-configuration.
 * EXTENSION POINTS: Could add cache statistics, bulk operations, cache patterns, or distributed locking capabilities.
 * THREAD SAFETY: ObjectMapper is thread-safe for configuration. RedisTemplate is thread-safe per Spring documentation.
 */
public class RedisService {

    @Autowired
    private RedisTemplate<String ,String > redisTemplate;

    public <T> T get(String key,  Class<T>entityClass){
        try {
            String dataFromRedis = redisTemplate.opsForValue().get(key);
            if (dataFromRedis==null){
                return null;
            }
            ObjectMapper mapper=new ObjectMapper();
            return mapper.readValue(dataFromRedis,entityClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void set(String key,Object object, Long tll){//ttl = valid till
        try {
            ObjectMapper mapper=new ObjectMapper();
            String anyValueToString= mapper.writeValueAsString(object);
            redisTemplate.opsForValue().set(key,anyValueToString,tll, TimeUnit.MINUTES);
        }
        catch (Exception e){
            throw  new RuntimeException(e);
        }
    }
    public void delete(String key){
        redisTemplate.delete(key);
    }
}
