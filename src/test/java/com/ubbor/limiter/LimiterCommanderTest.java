package com.ubbor.limiter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Callable;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/30
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LimiterCommanderTest {

    @Autowired
    LimiterCommander limiterCommander;

    @Test
    public void test() throws InterruptedException {
        Callable<String> call = () -> "abc";
        for (int i = 0; i < 100; i ++){
            LimiterCommanderResponse<String> response = limiterCommander.execute("abc", call);
            log.info("result:{}", JSON.toJSONString(response));
            Thread.sleep(2);
        }

    }

    @Test
    public void testBreaker() throws InterruptedException {
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw new RuntimeException("test exception");
            }
        };
        for (int i = 0; i < 100; i ++){
            LimiterCommanderResponse<String> response = limiterCommander.execute("abc", call);
            Thread.sleep(2);
//
//
        }

    }

    @Test
    public void bench() throws InterruptedException {
        long start = System.currentTimeMillis();
        Callable<String> call = () -> "abc";
        for (int i = 0; i < 1000; i ++){
            LimiterCommanderResponse<String> response = limiterCommander.execute("abc", call);
        }
        log.info("time:{}",System.currentTimeMillis() - start);
    //time:16983
        //16ms
    }

    @Test
    public void bench2() throws InterruptedException {
        long start = System.currentTimeMillis();
        Callable<String> call = () -> "abc";
        for (int i = 0; i < 1000; i ++){
            try {
                call.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("time:{}",System.currentTimeMillis() - start);
        // time:1
    }
}