package com.ubbor.limiter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/30
 */
@Slf4j
@Setter
@Getter
public class LimiterCommander {

    RedisClient<String, JSONObject> redisClient;
    LimiterConfigurationFactory limiterConfigurationFactory;

    LimiterCommander(RedisClient<String, JSONObject> redisClient, LimiterConfigurationFactory
            limiterConfigurationFactory) {
        this.redisClient = redisClient;
        this.limiterConfigurationFactory = limiterConfigurationFactory;
    }

    public <T> LimiterCommanderResponse<T> execute(String target, Callable<T> task) {
        LimiterConfiguration configuration = limiterConfigurationFactory.getConfiguration(target);
        LimiterCommanderResponse<T> response = new LimiterCommanderResponse<T>();
        LimiterCommanderStatus status = aquire(target, configuration);
        response.setStatus(status);
        if (LimiterCommanderStatus.OK.name().equals(status.name()) || LimiterCommanderStatus.TEST.name().equals
                (status.name())) {
            ExecuteResult executeResult = ExecuteResult.SUCCESS;
            try {
                response.setResult(task.call());
            } catch (Exception e) {
                executeResult = ExecuteResult.EXCEPTION;
                response.setStatus(LimiterCommanderStatus.EXCEPTION);
            } finally {
                String consume = consume(target, status, executeResult, configuration);
            }
        }
        return response;
    }

    private LimiterCommanderStatus aquire(String target, LimiterConfiguration config) {
//        DefaultRedisScript<JSONObject> getRedisScript = new DefaultRedisScript<>();
//        getRedisScript.setResultType(JSONObject.class);
//        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("require.lua")));
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(config.getKeyPrefix());
        keyList.add(target);

        /**
         * 调用脚本并执行
         */
        JSONObject result = redisClient.execute(new ClassPathResource("require.lua"),
                keyList, config.getMaxConcurrent(), config.getConcurrentWindow());
        log.info("redis aquire script result:{}", JSON.toJSONString(result));
        if (result != null && result.containsKey("status")) {
            return LimiterCommanderStatus.fromString(result.getString("status"));
        }
        return LimiterCommanderStatus.EXCEPTION;

    }

    private String consume(String target, LimiterCommanderStatus status, ExecuteResult executeResult, LimiterConfiguration config){
//        DefaultRedisScript<JSONObject> getRedisScript = new DefaultRedisScript<>();
//        getRedisScript.setResultType(JSONObject.class);
//        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("consume.lua")));
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(config.getKeyPrefix());
        keyList.add(target);

        JSONObject result = redisClient.execute(new ClassPathResource("consume.lua"),keyList,status.name(),executeResult.name(),config.getMaxConcurrent(),
                config.getBreakerOpenRate(),config.getBreakerOpenCount(),config.getBreakerWindow());
        log.info("redis consume script result:{}", JSON.toJSONString(result));
        return String.valueOf(result);

    }
}

@Setter
@Getter
class LimiterCommanderResponse<R> {
    LimiterCommanderStatus status;
    R result;
}

enum LimiterCommanderStatus {
    OK,
    LIMITER_REFUSED,
    BREAKER_OPEN,
    EXCEPTION,
    TEST;

    public static LimiterCommanderStatus fromString(String name) {
        for (LimiterCommanderStatus e : LimiterCommanderStatus.values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }
}

enum ExecuteResult {
    SUCCESS,
    EXCEPTION
}