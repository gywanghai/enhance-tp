package com.ocean.enhancetp.core.service.impl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ServiceLoaderUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventContext;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.common.event.DefaultEventPublisher;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.alarm.DefaultThreadPoolExecutorAlarmer;
import com.ocean.enhancetp.core.alarm.ThreadPoolExecutorAlarmer;
import com.ocean.enhancetp.core.alarm.AlarmEventListener;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.monitor.DefaultThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMetrics;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.vo.UpdateRecordVO;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@Slf4j
public class ThreadPoolExecutorServiceImpl implements ThreadPoolExecutorService {

    private Map<String, ThreadPoolExecutorWrapper> threadPoolExecutorWrapperMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService monitorExecutorService;

    private EventPublisher eventPublisher;

    private ThreadPoolExecutorMonitor threadPoolExecutorMonitor;

    private ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer;
    /**
     * 是否开启监控
     */
    private AtomicBoolean monitor = new AtomicBoolean(false);

    public ThreadPoolExecutorServiceImpl(){
        // 初始化事件监听器
        initEventPublisher();
        // 初始化线程池监控器
        initMonitor();
        // 初始化线程池报警器
        initAlarmer();
    }

    private void initAlarmer() {
        ThreadPoolExecutorAlarmer alarmer = ServiceLoaderUtil.loadFirst(ThreadPoolExecutorAlarmer.class);
        if(threadPoolExecutorAlarmer == null){
            this.threadPoolExecutorAlarmer = new DefaultThreadPoolExecutorAlarmer();
        }
        else {
            this.threadPoolExecutorAlarmer = alarmer;
        }
    }

    private void initMonitor() {
        monitorExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(
                "enhance-tp-monitor-", false
        ));
        ThreadPoolExecutorMonitor monitor = ServiceLoaderUtil.loadFirst(ThreadPoolExecutorMonitor.class);
        if(threadPoolExecutorMonitor == null){
            this.threadPoolExecutorMonitor = new DefaultThreadPoolExecutorMonitor();
        }
        else {
            this.threadPoolExecutorMonitor = monitor;
        }
    }

    private void initEventPublisher() {
        ServiceLoader<EventPublisher> serviceLoader = ServiceLoader.load(EventPublisher.class);
        if(serviceLoader.iterator().hasNext()){
            this.eventPublisher =  serviceLoader.iterator().next();
        }
        else {
            this.eventPublisher = new DefaultEventPublisher();
        }
        this.eventPublisher.registerEventListener(EventSource.ALARM.name(), new AlarmEventListener(this));
        EventContext.init(this.eventPublisher);
    }

    @Override
    public ThreadPoolExecutorWrapper getThreadPoolExecutorWrapper(String threadPoolId) {
        return threadPoolExecutorWrapperMap.get(threadPoolId);
    }

    @Override
    public void registerThreadPoolExecutorWrapper(ThreadPoolExecutorWrapper threadPoolExecutorWrapper) {
        ThreadPoolExecutorProperties properties = threadPoolExecutorWrapper.getProperties();
        threadPoolExecutorWrapperMap.put(properties.getThreadPoolId(), threadPoolExecutorWrapper);
    }

    @Override
    public void update(String threadPoolId, ThreadPoolExecutorProperties threadPoolExecutorProperties) {
        // 根据线程池 ID 查询线程池
        ThreadPoolExecutorWrapper executorWrapper = this.getThreadPoolExecutorWrapper(threadPoolId);
        executorWrapper.update(threadPoolExecutorProperties);
        executorWrapper.scrapeMetrics();

        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setThreadPoolId(threadPoolExecutorProperties.getThreadPoolId());
        alarmInfo.setApplication(threadPoolExecutorProperties.getApplication());
        alarmInfo.setNamespace(threadPoolExecutorProperties.getNamespace());
        alarmInfo.setDate(new Date());
        alarmInfo.setData(new UpdateRecordVO(executorWrapper.getProperties(), threadPoolExecutorProperties));
        alarmInfo.setAlarmType(AlarmType.CONFIG_UPDATE);

        eventPublisher.publishEvent(new Event<AlarmInfo>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo ,new Date()));
    }

    @Override
    public void startMonitor() {
        if(monitor.compareAndSet(false, true)){
            monitorExecutorService.submit(new MonitorTask());
        }
    }

    @Override
    public ThreadPoolExecutorWrapper getThreadPoolExecutorWrapper(ThreadPoolExecutor executor) {
        Optional<Map.Entry<String, ThreadPoolExecutorWrapper>> optional =  threadPoolExecutorWrapperMap.entrySet().
                stream().filter(wrapperEntry -> wrapperEntry.getValue().getExecutor() == executor).findFirst();
        if(optional.isPresent()){
            return optional.get().getValue();
        }
        return null;
    }

    @Override
    public void increaseRejectedCount(String threadPoolId) {
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = threadPoolExecutorWrapperMap.get(threadPoolId);
        if(threadPoolExecutorWrapper != null){
            ThreadPoolExecutorMetrics metrics = threadPoolExecutorWrapper.getMetrics();
            metrics.getRejectedCount().incrementAndGet();
            threadPoolExecutorWrapper.scrapeMetrics();
        }
    }

    @Override
    public void increaseFailCount(String threadPoolId) {
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = threadPoolExecutorWrapperMap.get(threadPoolId);
        if(threadPoolExecutorWrapper != null){
            ThreadPoolExecutorMetrics metrics = threadPoolExecutorWrapper.getMetrics();
            metrics.getExecuteFailCount().incrementAndGet();
            threadPoolExecutorWrapper.scrapeMetrics();
        }
    }

    @Override
    public void increaseExecTimeoutCount(String threadPoolId) {
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = threadPoolExecutorWrapperMap.get(threadPoolId);
        if(threadPoolExecutorWrapper != null){
            ThreadPoolExecutorMetrics metrics = threadPoolExecutorWrapper.getMetrics();
            metrics.getExecuteTimeoutCount().incrementAndGet();
            threadPoolExecutorWrapper.scrapeMetrics();
        }
    }

    public void destroy(){
        log.info("关闭所有线程池");
        threadPoolExecutorWrapperMap.entrySet().stream().forEach(wrapperEntry -> wrapperEntry.getValue().destory());
        this.monitorExecutorService.shutdown();
    }

    class MonitorTask implements Runnable {
        @Override
        public void run() {
            threadPoolExecutorWrapperMap.values().stream().forEach(threadPoolExecutorWrapper -> {
                threadPoolExecutorWrapper.scrapeMetrics();
                threadPoolExecutorMonitor.monitor(threadPoolExecutorWrapper.getMetrics());
                threadPoolExecutorAlarmer.alarm(threadPoolExecutorWrapper.getMetrics(), threadPoolExecutorWrapper.getProperties());
            });
            monitorExecutorService.schedule(this, 5, TimeUnit.SECONDS);
        }
    }
}
