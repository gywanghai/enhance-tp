package com.ocean.enhancetp.core.monitor;

import cn.hutool.core.thread.NamedThreadFactory;
import com.ocean.enhancetp.core.alarm.ThreadPoolExecutorAlarmer;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private ThreadPoolExecutorService threadPoolExecutorService;


    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer;


    public SimpleThreadPoolExecutorMonitor(ThreadPoolExecutorService threadPoolExecutorService, ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer){
        this.threadPoolExecutorService = threadPoolExecutorService;
        this.threadPoolExecutorAlarmer = threadPoolExecutorAlarmer;
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("enhance-tp-monitor-",true));
    }

    @Override
    public void startMonitor() {
        if(monitor.compareAndSet(false, true)){
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new MonitorTask(),0, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void monitor(ThreadPoolExecutorMetrics metrics) {
        log.info("线程池监控信息: {}", metrics);
        tagsMap.computeIfAbsent(metrics.getThreadPoolId(), s -> {
            List<Tag> tagList = new ArrayList<>();
            tagList.add(Tag.of("threadPoolId", metrics.getThreadPoolId()));
            return tagList;
        });
        List<Tag> tags = tagsMap.get(metrics.getThreadPoolId());
        Metrics.globalRegistry.gauge("threadpool.core.poolsize", tags, metrics, ThreadPoolExecutorMetrics::getCorePoolSize);
        Metrics.globalRegistry.gauge("threadpool.maximum.poolsize", tags, metrics, ThreadPoolExecutorMetrics::getPoolSize);
        Metrics.globalRegistry.gauge("threadpool.active.count", tags, metrics, value -> value.getActiveCount().longValue());
        Metrics.globalRegistry.gauge("threadpool.poolsize", tags, metrics, value -> value.getPoolSize().longValue());
        Metrics.globalRegistry.gauge("threadpool.largest.poolsize", tags, metrics, value -> value.getLargestPoolSize().longValue());
        Metrics.globalRegistry.gauge("threadpool.completedtask.count", tags, metrics, ThreadPoolExecutorMetrics::getCompletedTaskCount);
        Metrics.globalRegistry.gauge("threadpool.rejected.count", tags, metrics, value -> value.getRejectedCount().longValue());
        Metrics.globalRegistry.gauge("threadpool.fail.count", tags, metrics, value -> value.getExecuteFailCount().longValue());
        Metrics.globalRegistry.gauge("threadpool.timeout.count", tags, metrics, value -> value.getExecuteTimeoutCount().longValue());
        Metrics.globalRegistry.gauge("threadpool.task.count", tags, metrics, ThreadPoolExecutorMetrics::getTaskCount);
        Metrics.globalRegistry.gauge("threadpool.queue.size", tags, metrics, value -> value.getWorkQueueSize().longValue());
        Metrics.globalRegistry.gauge("threadpool.wait.timeout.count", tags, metrics, value -> value.getWaitTimeoutCount().longValue());
    }

    public class MonitorTask implements Runnable {
        @Override
        public void run() {
            try{
                Collection<ThreadPoolExecutorWrapper> threadPoolExecutorWrappers = threadPoolExecutorService.getAllThreadPoolExecutorWrapper();
                threadPoolExecutorWrappers.stream().forEach(threadPoolExecutorWrapper -> {
                    threadPoolExecutorWrapper.scrapeMetrics();
                    SimpleThreadPoolExecutorMonitor.this.monitor(threadPoolExecutorWrapper.getMetrics());
                    threadPoolExecutorAlarmer.alarm(threadPoolExecutorWrapper.getMetrics(), threadPoolExecutorWrapper.getProperty());
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
