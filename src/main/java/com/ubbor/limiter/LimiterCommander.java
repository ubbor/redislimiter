package com.ubbor.limiter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/30
 */
@Slf4j
@Service
public class LimiterCommander {
    @Autowired
    RedisLimiterService redisLimiterService;



    public <T> LimiterCommanderResponse<T> execute(String target, Callable<T> task) {
        LimiterCommanderResponse<T> response = new LimiterCommanderResponse<T>();
        LimiterCommanderStatus status = redisLimiterService.aquire(target,getConfig());
        response.setStatus(status);
        if (LimiterCommanderStatus.OK.name().equals(status.name()) || LimiterCommanderStatus.TEST.name().equals(status.name())) {
            ExecuteResult executeResult = ExecuteResult.SUCCESS;
            try {
                response.setResult(task.call());
            } catch (Exception e) {
                executeResult = ExecuteResult.EXCEPTION;
                response.setStatus(LimiterCommanderStatus.EXCEPTION);
            } finally {
                String consume = redisLimiterService.consume(target,status, executeResult,getConfig());
            }
        }
        return response;
    }

    private LimiterConfigureDto getConfig(){
        LimiterConfigureDto configureDto = new LimiterConfigureDto();
        configureDto.setKeyPrefix("ubbor:limiter");
        configureDto.setBreakerOpenCount(10);
        configureDto.setBreakerOpenRate(80);
        configureDto.setBreakerWindow(60);
        configureDto.setMaxConcurrent(100);
        configureDto.setConcurrentWindow(60);
        return configureDto;
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

    public static LimiterCommanderStatus fromString(String name){
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