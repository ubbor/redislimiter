package com.ubbor.limiter;

import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * @author ericprotectearth
 * @date Created on 2019/8/7
 */
public class RedisClientTest implements RedisClient<String, JSONObject> {

    protected RedisTemplate<String, Object> redisTemplate;

    @Override
    public JSONObject execute(ClassPathResource scriptResource, List<String> keys, Object... args) {
        DefaultRedisScript<JSONObject> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(JSONObject.class);
        redisScript.setScriptSource(new ResourceScriptSource(scriptResource));
        return redisTemplate.execute(redisScript, keys, args);
    }

    public RedisClientTest() {
        JedisConnectionFactory conn = new JedisConnectionFactory();
        //JedisConnectionFactory conn = new JedisConnectionFactory();
        conn.setDatabase(0);
        conn.setHostName("127.0.0.1");
        conn.setPort(6379);
        conn.setPassword("");
        conn.setUsePool(true);
        conn.afterPropertiesSet();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(conn);
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<Object>(Object.class);
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        this.redisTemplate = redisTemplate;
    }
}
