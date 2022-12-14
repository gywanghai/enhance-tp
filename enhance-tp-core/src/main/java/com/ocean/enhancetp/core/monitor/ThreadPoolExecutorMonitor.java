package com.ocean.enhancetp.core.monitor;

/**
 * @description: 线程池监控器接口
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface ThreadPoolExecutorMonitor {

    /**
     * 开启监控
     */
    public void startMonitor();

    /**
     * 监控性能指标
     * @param metrics
     */
    public void monitor(ThreadPoolExecutorMetrics metrics);

}
