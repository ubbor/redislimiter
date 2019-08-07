package com.ubbor.limiter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author ericprotectearth@gmail.com
 * @date Created on 2019/7/31
 */
@Setter
@Getter
public class LimiterConfiguration implements Serializable{
    private static final long serialVersionUID = -6334839695743177588L;
    /**
     * redis key前缀
     */
    String keyPrefix;
    /**
     * 最大并发数
     */
    int maxConcurrent;
    /**
     * 并发时间窗口
     */
    int concurrentWindow;
    /**
     * 熔断条件1,错误百分比分母
     */
    int breakerOpenRate ;
    /**
     * 熔断条件2,窗口期内的错误数
     */
    int breakerOpenCount ;
    /**
     * 熔断条件3,窗口时间长度,单位秒
     */
    int breakerWindow ;
}
