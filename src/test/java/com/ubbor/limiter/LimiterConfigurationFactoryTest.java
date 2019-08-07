package com.ubbor.limiter;

/**
 * @author ericprotectearth
 * @date Created on 2019/8/7
 */
public class LimiterConfigurationFactoryTest implements LimiterConfigurationFactory {
    @Override
    public LimiterConfiguration getConfiguration(String target) {
        return StandaloneConfiguration();
    }

    private LimiterConfiguration StandaloneConfiguration() {
        LimiterConfiguration configureDto = new LimiterConfiguration();
        configureDto.setKeyPrefix("ubbor:limiter");
        configureDto.setBreakerOpenCount(10);
        configureDto.setBreakerOpenRate(80);
        configureDto.setBreakerWindow(60);
        configureDto.setMaxConcurrent(100);
        configureDto.setConcurrentWindow(60);
        return configureDto;
    }
}
