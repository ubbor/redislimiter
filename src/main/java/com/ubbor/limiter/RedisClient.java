package com.ubbor.limiter;

import org.springframework.core.io.ClassPathResource;

import java.util.List;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/8/7
 */
public interface RedisClient<String, JSONObject> {
    /**
     * excute lua script
     *
     * @param scriptResource
     * @param keys
     * @param args
     * @return JSONObject
     */
    JSONObject execute(ClassPathResource scriptResource, List<String> keys, Object... args);
}
