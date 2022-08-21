package com.ocean.enhancetp.core.monitor;

import cn.hutool.core.thread.NamedThreadFactory;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 默认线程池监控器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/17
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class SimpleThreadPoolExecutorMonitor implements ThreadPoolExecutorMonitor{

    /**
     * 是否开启监控
     */
    private AtomicBoolean monitor = new AtomicBoolean(false);

    private Map<String, List<Tag>> tagsMap = new ConcurrentHashMap<>();

    private MeterRegistry meterRegistry;

    private ThreadPoolExecutorService threadPoolExecutorService;


    private ScheduledExecutorService monitorExecutorService;

    public SimpleThreadPoolExecutorMonitor(MeterRegistry meterRegistry, ThreadPoolExecutorService threadPoolExecutorService){
        this.meterRegistry = meterRegistry;
        this.threadPoolExecutorService = threadPoolExecutorService;
        this.monitorExecutorService = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("enhance-tp-monitor",false));
    }

    @Override
    public void startMonitor() {
        if(monitor.compareAndSet(false, true)){
            monitorExecutorService.submit(new MonitorTask());
        }
    }

    @Override
    public void monitor(ThreadPoolExecutorMetrics metrics) {
        log.info("线程池监控信息: {}", metrics);
//        tagsMap.computeIfAbsent(metrics.getThreadPoolId(), new Function<String, List<Tag>>() {
//            @Override
//            public List<Tag> apply(String s) {
//                List<Tag> tagList = new ArrayList<>();
//                tagList.add(Tag.of("application", metrics.getApplication()));
//                tagList.add(Tag.of("namespace", metrics.getNamespace()));
//                tagList.add(Tag.of("threadPoolId", metrics.getThreadPoolId()));
//                return tagList;
//            }
//        });
//        List<Tag> tags = tagsMap.get(metrics.getThreadPoolId());
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("threadPoolId", metrics.getThreadPoolId()));
        meterRegistry.gauge("threadpool.core.poolsize", tags, metrics, value -> value.getCorePoolSize());
        meterRegistry.gauge("threadpool.maximum.poolsize", tags, metrics, value -> value.getPoolSize());
        meterRegistry.gauge("threadpool.active.count", tags, metrics, value -> value.getActiveCount().longValue());
        meterRegistry.gauge("threadpool.poolsize", tags, metrics, value -> value.getPoolSize().longValue());
        meterRegistry.gauge("threadpool.largest.poolsize", tags, metrics, value -> value.getLargestPoolSize().longValue());
        meterRegistry.gauge("threadpool.completedtask.count", tags, metrics, value -> value.getCompletedTaskCount().longValue());
        meterRegistry.gauge("threadpool.rejected.count", tags, metrics, value -> value.getRejectedCount().longValue());
        meterRegistry.gauge("threadpool.fail.count", tags, metrics, value -> value.getExecuteFailCount().longValue());
        meterRegistry.gauge("threadpool.timeout.count", tags, metrics, value -> value.getExecuteTimeoutCount().longValue());
        meterRegistry.gauge("threadpool.task.count", tags, metrics, value -> value.getTaskCount().longValue());
        meterRegistry.gauge("threadpool.queue.size", tags, metrics, value -> value.getWorkQueueSize().longValue());
    }

    public class MonitorTask implements Runnable {
        @Override
        public void run() {
            Collection<ThreadPoolExecutorWrapper> threadPoolExecutorWrappers = threadPoolExecutorService.getAllThreadPoolExecutorWrapper();
            threadPoolExecutorWrappers.stream().forEach(threadPoolExecutorWrapper -> {
               threadPoolExecutorWrapper.scrapeMetrics();
               SimpleThreadPoolExecutorMonitor.this.monitor(threadPoolExecutorWrapper.getMetrics());
            });
            monitorExecutorService.schedule(this, 5, TimeUnit.SECONDS);
        }
    }
}
