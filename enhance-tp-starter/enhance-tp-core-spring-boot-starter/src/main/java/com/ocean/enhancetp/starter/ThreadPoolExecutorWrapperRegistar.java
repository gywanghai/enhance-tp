package com.ocean.enhancetp.starter;

import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/23
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class ThreadPoolExecutorWrapperRegistar implements SmartInitializingSingleton, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ThreadPoolExecutorService threadPoolExecutorService;

    private ThreadPoolExecutorMonitor threadPoolExecutorMonitor;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ThreadPoolExecutorWrapperRegistar(ThreadPoolExecutorService threadPoolExecutorService, ThreadPoolExecutorMonitor threadPoolExecutorMonitor){
        this.threadPoolExecutorMonitor = threadPoolExecutorMonitor;
        this.threadPoolExecutorService = threadPoolExecutorService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = applicationContext.getBeanNamesForType(ThreadPoolExecutorWrapper.class);
        if(beanNames.length == 0){
            return;
        }
        Arrays.stream(beanNames).forEach(beanName -> {
            threadPoolExecutorService.registerThreadPoolExecutorWrapper((ThreadPoolExecutorWrapper) applicationContext.getBean(beanName));
        });
        threadPoolExecutorMonitor.startMonitor();
    }
}
