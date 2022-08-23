package com.ocean.enhancetp.starter;

import com.ocean.enhancetp.core.alarm.DefaultThreadPoolExecutorAlarmer;
import com.ocean.enhancetp.core.alarm.ThreadPoolExecutorAlarmer;
import com.ocean.enhancetp.core.monitor.SimpleThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.service.impl.ThreadPoolExecutorServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class EnhanceTpCoreAutoConfiguration {

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
    @ConditionalOnMissingBean(ThreadPoolExecutorAlarmer.class)
    public ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer(){
        return new DefaultThreadPoolExecutorAlarmer();
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
