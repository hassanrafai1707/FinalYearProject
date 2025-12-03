package com.FinalYearProject.FinalYearProject.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
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
