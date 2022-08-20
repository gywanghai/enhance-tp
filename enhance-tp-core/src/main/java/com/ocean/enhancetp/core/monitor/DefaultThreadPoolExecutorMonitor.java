package com.ocean.enhancetp.core.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * @description: 默认线程池监控器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/17
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class DefaultThreadPoolExecutorMonitor implements ThreadPoolExecutorMonitor{

    @Override
    public void monitor(ThreadPoolExecutorMetrics metrics) {
        log.info("线程池状态监控: {}", metrics);
    }
}
