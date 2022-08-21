package com.ocean.enhancetp.example;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.blockingqueue.ResizableLinkedBlockingQueue;
import com.ocean.enhancetp.core.context.RunnableContext;
import com.ocean.enhancetp.core.monitor.SimpleThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.service.impl.ThreadPoolExecutorServiceImpl;
import com.ocean.enhancetp.core.wrapper.RunnableWrapper;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Component
public class EnhanceThreadPoolTest implements ApplicationRunner {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                testCreateThreadPoolFromProperties();
            }
        }).start();
    }

    private void testCreateThreadPoolFromProperties() {
        String threadPoolId = "test2";
        ThreadPoolExecutorProperties threadPoolExecutorProperties = new ThreadPoolExecutorProperties();
        threadPoolExecutorProperties.setThreadPoolId(threadPoolId);
        threadPoolExecutorProperties.setClassName(ThreadPoolExecutor.class.getName());
        threadPoolExecutorProperties.setRejectedExecutionHandlerClassName(ThreadPoolExecutor.DiscardOldestPolicy.class.getName());
        threadPoolExecutorProperties.setWorkQueueClassName(ResizableLinkedBlockingQueue.class.getName());
        threadPoolExecutorProperties.setKeepAliveTime(60L);
        threadPoolExecutorProperties.setWorkQueueCapacity(1);
        threadPoolExecutorProperties.setAllowCoreThreadTimeOut(false);
        threadPoolExecutorProperties.setCorePoolSize(1);
        threadPoolExecutorProperties.setMaximumPoolSize(1);

        Map<String, Number> alarmThreshold = new ConcurrentHashMap<>();
        alarmThreshold.put(AlarmType.BLOCKING_QUEUE_SIZE.name(), 1000);
        alarmThreshold.put(AlarmType.THREADPOOL_LIVENESS.name(), 8);
        alarmThreshold.put(AlarmType.TASK_EXECUTION_TIMEOUT.name(), 1000);

        threadPoolExecutorProperties.setAlarmThreshold(alarmThreshold);

        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = new ThreadPoolExecutorWrapper(threadPoolExecutorProperties);

        ThreadPoolExecutorService threadPoolExecutorService = new ThreadPoolExecutorServiceImpl();
        threadPoolExecutorService.registerThreadPoolExecutorWrapper(threadPoolExecutorWrapper);

        ThreadPoolExecutorMonitor threadPoolExecutorMonitor = new SimpleThreadPoolExecutorMonitor(meterRegistry,
                threadPoolExecutorService);
        threadPoolExecutorMonitor.startMonitor();

        TransmittableThreadLocal<RunnableContext> context = new TransmittableThreadLocal<>();
        context.set(new RunnableContext(threadPoolExecutorWrapper, new Date()));

        for (int i = 0; i < 100; i++){
            threadPoolExecutorWrapper.getExecutor().submit(new RunnableWrapper(new Runnable() {
                @Override
                public void run() {
                    System.out.println(context.get());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if(System.currentTimeMillis() % 2 == 0){
                        System.out.println(1/0);
                    }
                }
            },context));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
