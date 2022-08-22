package com.ocean.enhancetp.core.monitor;

import lombok.Data;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @description: 线程池运行状态指标
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
public class ThreadPoolExecutorMetrics {
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
     * 工作队列大小
     */
    private Integer workQueueCapacity;
    /**
     * 工作队列长度
     */
    private Integer workQueueSize;
    /**
     * 已提交任务数
     */
    private Long taskCount;
    /**
     * 当前线程池大小
     */
    private Integer poolSize;
    /**
     * 正在运行的线程的大小
     */
    private Integer activeCount;
    /**
     * 线程池最大线程数
     */
    private Integer largestPoolSize;
    /**
     * 已完成任务数
     */
    private Long completedTaskCount;
    /**
     * 被拒绝任务数
     */
    private AtomicLong rejectedCount;
    /**
     * 执行超时次数
     */
    private AtomicLong executeTimeoutCount;
    /**
     * 任务执行失败次数
     */
    private AtomicLong executeFailCount;
    /**
     * 等待超时次数
     */
    private AtomicLong waitTimeoutCount;
}
