package com.ocean.enhancetp.autoconfig.configuration;

import com.ocean.enhancetp.autoconfig.processor.EnhanceThreadPoolBeanFactoryPostProcessor;
import com.ocean.enhancetp.autoconfig.processor.EnhanceThreadPoolBeanPostProcessor;
import com.ocean.enhancetp.autoconfig.refresher.ThreadPoolExecutorRefresher;
import com.ocean.enhancetp.autoconfig.registrar.ThreadPoolExecutorWrapperRegistar;
import com.ocean.enhancetp.common.event.DefaultEventPublisher;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.core.alarm.*;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.monitor.SimpleThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.service.impl.ThreadPoolExecutorServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class EnhanceTpCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(EnhanceThreadPoolBeanFactoryPostProcessor.class)
    public EnhanceThreadPoolBeanFactoryPostProcessor enhanceThreadPoolBeanFactoryPostProcessor(){
        return new EnhanceThreadPoolBeanFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(EnhanceThreadPoolBeanPostProcessor.class)
    public EnhanceThreadPoolBeanPostProcessor enhanceThreadPoolBeanPostProcessor(){
        return new EnhanceThreadPoolBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutorService.class)
    public ThreadPoolExecutorService threadPoolExecutorService(){
        return new ThreadPoolExecutorServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(AlarmEventListener.class)
    public AlarmEventListener alarmEventListener(ThreadPoolExecutorService threadPoolExecutorService){
        AlarmEventListener alarmEventListener = new AlarmEventListener(threadPoolExecutorService);
        alarmEventListener.registerHandler(AlarmType.WAIT_TIMEOUT, new TaskWaitTimeoutAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.TASK_REJECTED, new RejectedExecutionAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.CONFIG_UPDATE, new ConfigUpdateAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.TASK_FAIL, new TaskFailAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.BLOCKING_QUEUE_SIZE, new BlockingQueueSizeAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.TASK_EXECUTION_TIMEOUT, new TaskExecutionTimeoutAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.THREADPOOL_LIVENESS, new ThreadPoolLivenessAlarmHandler());
        return alarmEventListener;
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutorAlarmer.class)
    public ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer(EventPublisher eventPublisher){
        return new DefaultThreadPoolExecutorAlarmer(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher eventPublisher(AlarmEventListener alarmEventListener){
        EventPublisher eventPublisher = new DefaultEventPublisher();
        eventPublisher.registerEventListener(EventSource.ALARM.name(), alarmEventListener);
        return eventPublisher;
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutorRefresher.class)
    public ThreadPoolExecutorRefresher threadPoolExecutorRefresher(ThreadPoolExecutorService threadPoolExecutorService, EventPublisher eventPublisher){
        return new ThreadPoolExecutorRefresher(threadPoolExecutorService, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutorMonitor.class)
    public ThreadPoolExecutorMonitor threadPoolExecutorMonitor(ThreadPoolExecutorService threadPoolExecutorService, ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer){
        return new SimpleThreadPoolExecutorMonitor(threadPoolExecutorService, threadPoolExecutorAlarmer);
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutorWrapperRegistar.class)
    public ThreadPoolExecutorWrapperRegistar threadPoolExecutorWrapperRegistar(ThreadPoolExecutorService threadPoolExecutorService, ThreadPoolExecutorMonitor threadPoolExecutorMonitor){
        return new ThreadPoolExecutorWrapperRegistar(threadPoolExecutorService, threadPoolExecutorMonitor);
    }
}
