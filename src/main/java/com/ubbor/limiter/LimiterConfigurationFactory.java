package com.ubbor.limiter;

/**
 * @author ericprotectearth
 * @date Created on 2019/8/7
 */
public interface LimiterConfigurationFactory {
    /**
     * Get Configguration
     *
     * @param target
     * @return
     */
    LimiterConfiguration getConfiguration(String target);
}
