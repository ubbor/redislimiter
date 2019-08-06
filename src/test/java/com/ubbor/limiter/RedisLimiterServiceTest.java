package com.ubbor.limiter;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/25
 */



@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisLimiterServiceTest {
    @Autowired
    RedisLimiterService redisLimiterService;

    @Test
    public void aquire(){
        redisLimiterService.aquire("PICC",new LimiterConfigureDto());
    }

    @Test
    public void consume(){
        //redisLimiterService.consume("PICC",null);
    }
}