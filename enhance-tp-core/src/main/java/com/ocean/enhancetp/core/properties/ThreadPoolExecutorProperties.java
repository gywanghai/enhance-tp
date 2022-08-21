package com.ocean.enhancetp.core.properties;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description: 线程池属性
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
public class ThreadPoolExecutorProperties {
    /**
     * 线程池 ID
     */
    private String threadPoolId;
    /**
     * 核心线程数
     */
    private Integer corePoolSize;
    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;
    /**
     * 存活时间，单位：second
     */
    private Long keepAliveTime = 0L;
    /**
     * 工作队列类型
     */
    private String workQueueClassName;
    /**
     * 工作队列容量
     */
    private Integer workQueueCapacity = 0;
    /**
     * 拒绝策略
     */
    private String rejectedExecutionHandlerClassName;
    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeOut = Boolean.FALSE;
    /**
     * 报警阈值 Map
     */
    private Map<String, Number> alarmThreshold = new HashMap<>();
    /**
     * 线程池类型名称，默认是普通的线程池
     */
    private String className = ThreadPoolExecutor.class.getName();
}
