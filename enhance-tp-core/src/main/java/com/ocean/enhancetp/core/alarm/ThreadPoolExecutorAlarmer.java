package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMetrics;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;

/**
 * @description: 线程池告警器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface ThreadPoolExecutorAlarmer {

    public void alarm(ThreadPoolExecutorMetrics threadPoolExecutorMetrics, ThreadPoolExecutorProperty properties);

}
