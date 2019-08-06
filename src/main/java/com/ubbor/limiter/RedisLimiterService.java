package com.ubbor.limiter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/25
 */
@Slf4j
@Service
public class RedisLimiterService {

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    public LimiterCommanderStatus aquire(String target,LimiterConfigureDto config){
        DefaultRedisScript<JSONObject> getRedisScript = new DefaultRedisScript<>();
        getRedisScript.setResultType(JSONObject.class);
        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("require.lua")));
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(config.getKeyPrefix());
        keyList.add(target);

        /**
         * 调用脚本并执行
         */
        JSONObject result = redisTemplate.execute(getRedisScript,keyList,config.getMaxConcurrent(),config.getConcurrentWindow());
        log.info("redis aquire script result:{}", JSON.toJSONString(result));
        if (result != null && result.containsKey("status")){
            return LimiterCommanderStatus.fromString(result.getString("status"));
        }
        return LimiterCommanderStatus.EXCEPTION;

    }


    public String consume(String target, LimiterCommanderStatus status, ExecuteResult executeResult, LimiterConfigureDto config){
        DefaultRedisScript<JSONObject> getRedisScript = new DefaultRedisScript<>();
        getRedisScript.setResultType(JSONObject.class);
        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("consume.lua")));
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(config.getKeyPrefix());
        keyList.add(target);

        /**
         * 调用脚本并执行
         * -- 资源标识
         local target = KEYS[1]
         -- 请求时拿到的状态
         local require_status = ARGV[1]
         -- 执行状态 SUCCESS/EXCEPTION
         local execute_result = ARGV[2]
         -- 时间窗口内最大并发数
         local max_concurrent = tonumber(ARGV[3] or 0)
         -- 触发熔断的错误率
         local breaker_open_rate = tonumber(ARGV[4] or 1)
         -- 触发熔断时的最小错误数
         local breaker_open_count = tonumber(ARGV[5] or 0)
         -- 时间窗口大小 单位秒
         local window_second = tonumber(ARGV[6] or 0)
         */
        String require_status = "OK";
        String execute_result = "EXCEPTION";//EXCEPTION SUCCESS
        int max_concurrent = 1000;
        int breaker_open_rate = 70;//用百分比
        int breaker_open_count = 10;
        int window_second = 60;
        JSONObject result = redisTemplate.execute(getRedisScript,keyList,status.name(),executeResult.name(),config.getMaxConcurrent(),
                config.getBreakerOpenRate(),config.getBreakerOpenCount(),config.getBreakerWindow());
        log.info("redis consume script result:{}", JSON.toJSONString(result));
        return String.valueOf(result);

    }
}
