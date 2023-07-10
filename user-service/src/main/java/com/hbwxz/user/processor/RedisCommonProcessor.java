package com.hbwxz.user.processor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisCommonProcessor {

    @Autowired
    private RedisTemplate redisTemplate;

    public Object get (String key) {
        if (StringUtils.isEmpty(key)) {
            throw new UnsupportedOperationException("key can not be nul");
        }
        return redisTemplate.opsForValue().get(key);
    }

    public void set (String key , Object value) {
        if (StringUtils.isEmpty(key)) {
            throw new UnsupportedOperationException("key can not be nul");
        }
        redisTemplate.opsForValue().set(key , value);
    }

    public void set (String key , Object value , Long timeSeconds) {
        if (StringUtils.isEmpty(key)) {
            throw new UnsupportedOperationException("key can not be nul");
        }
        if (timeSeconds > 0) {
            redisTemplate.opsForValue().set(key , value , timeSeconds , TimeUnit.SECONDS);
        } else {
            set (key , value);
        }
    }

    public void remove (String key) {
        redisTemplate.delete(key);
    }
}
