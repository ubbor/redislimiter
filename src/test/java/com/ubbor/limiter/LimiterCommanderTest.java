package com.ubbor.limiter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/30
 */
@RunWith(Parameterized.class)
@Slf4j
public class LimiterCommanderTest {
    @Autowired
    LimiterCommander limiterCommander;

    public LimiterCommanderTest(LimiterCommander limiterCommander) {
        this.limiterCommander = limiterCommander;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testParams() {
        LimiterConfigurationFactory configurationFactory = new LimiterConfigurationFactoryTest();
        RedisClient<String, JSONObject> redisClient = new RedisClientTest();
        LimiterCommander limiterCommander = new LimiterCommander(redisClient, configurationFactory);
        return Arrays.asList(new Object[][]{
                {limiterCommander}
        });
    }

    @Test
    public void testNormal() throws InterruptedException {
        Callable<String> call = () -> "abc";
        for (int i = 0; i < 100; i++) {
            LimiterCommanderResponse<String> response = limiterCommander.execute("abc", call);
            log.info("result:{}", JSON.toJSONString(response));
            Thread.sleep(2);
        }

    }

    @Test
    public void testBreaker() throws InterruptedException {
        Callable<String> call = () -> {
            throw new RuntimeException("test exception");
        };
        for (int i = 0; i < 100; i++) {
            LimiterCommanderResponse<String> response = limiterCommander.execute("abc", call);
            log.info("result:{}", JSON.toJSONString(response));
            Thread.sleep(2);
        }

    }

    @Test
    public void testCommanderBenchmark() {
        long start = System.currentTimeMillis();
        Callable<String> call = () -> "abc";
        for (int i = 0; i < 1000; i++) {
            LimiterCommanderResponse<String> response = limiterCommander.execute("abc", call);
        }
        log.info("time:{}", System.currentTimeMillis() - start);
    }

    @Test
    public void benchmark() {
        long start = System.currentTimeMillis();
        Callable<String> call = () -> "abc";
        for (int i = 0; i < 1000; i++) {
            try {
                call.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("time:{}", System.currentTimeMillis() - start);
    }
}