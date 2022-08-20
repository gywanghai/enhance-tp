package com.ocean.enhancetp.core.service;

import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description: 线程池服务接口
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface ThreadPoolExecutorService {

    /**
     * 根据线程池 ID 查找线程池
     * @param threadPoolId 线程池 ID
     * @return
     */
    public ThreadPoolExecutorWrapper getThreadPoolExecutorWrapper(String threadPoolId);

    /**
     * 注册线程池
     * @param threadPoolExecutorWrapper 线程池装饰器
     */
    public void registerThreadPoolExecutorWrapper(ThreadPoolExecutorWrapper threadPoolExecutorWrapper);

    /**
     * 修改线程池参数
     * @param threadPoolId 线程池ID
     * @param threadPoolExecutorProperties 线程池参数
     */
    public void update(String threadPoolId, ThreadPoolExecutorProperties threadPoolExecutorProperties);

    /**
     * 开启监控
     */
    public void startMonitor();

    /**
     * 获取线程池
     * @param executor 线程池
     * @return
     */
    ThreadPoolExecutorWrapper getThreadPoolExecutorWrapper(ThreadPoolExecutor executor);

    /**
     * 任务被拒绝次数递增1
     * @param threadPoolId 线程池ID
     */
    void increaseRejectedCount(String threadPoolId);

    /**
     * 任务执行失败次数递增1
     */
    void increaseFailCount(String threadPoolId);

    /**
     * 任务执行超时次数递增1
     * @param threadPoolId
     */
    void increaseExecTimeoutCount(String threadPoolId);
}
