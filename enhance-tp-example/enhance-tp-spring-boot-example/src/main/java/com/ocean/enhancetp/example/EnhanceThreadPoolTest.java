package com.ocean.enhancetp.example;

import com.ocean.enhancetp.common.event.DefaultEventPublisher;
import com.ocean.enhancetp.common.event.EventContext;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.core.alarm.*;
import com.ocean.enhancetp.core.blockingqueue.ResizableLinkedBlockingQueue;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.monitor.SimpleThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMonitor;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.service.impl.ThreadPoolExecutorServiceImpl;
import com.ocean.enhancetp.core.wrapper.RunnableWrapper;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.security.SecureRandom;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
//@Component
public class EnhanceThreadPoolTest implements ApplicationRunner {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(this::testCreateThreadPoolFromProperties).start();
    }

    private void testCreateThreadPoolFromProperties() {
        // 构建线程池
        ThreadPoolExecutorProperties threadPoolExecutorProperties = buildThreadPoolExecutorProperties();
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = new ThreadPoolExecutorWrapper(threadPoolExecutorProperties);
        // 创建建线程池服务
        ThreadPoolExecutorService threadPoolExecutorService = new ThreadPoolExecutorServiceImpl();
        // 注册线程池
        threadPoolExecutorService.registerThreadPoolExecutorWrapper(threadPoolExecutorWrapper);
        // 创建线程池报警器
        ThreadPoolExecutorAlarmer threadPoolExecutorAlarmer = new DefaultThreadPoolExecutorAlarmer();
        // 创建线程池监控器
        ThreadPoolExecutorMonitor threadPoolExecutorMonitor = new SimpleThreadPoolExecutorMonitor(threadPoolExecutorService, threadPoolExecutorAlarmer);
        threadPoolExecutorMonitor.startMonitor();

        // 初始化事件发布器
        EventPublisher eventPublisher = initEventPublisher();
        /**
         * 注册报警处理器
         */
        AlarmEventListener alarmEventListener = new AlarmEventListener(threadPoolExecutorService);
        alarmEventListener.registerHandler(AlarmType.WAIT_TIMEOUT, new TaskWaitTimeoutAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.TASK_REJECTED, new RejectedExecutionAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.CONFIG_UPDATE, new ConfigUpdateAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.TASK_FAIL, new TaskFailAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.BLOCKING_QUEUE_SIZE, new BlockingQueueSizeAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.TASK_EXECUTION_TIMEOUT, new TaskExecutionTimeoutAlarmHandler());
        alarmEventListener.registerHandler(AlarmType.THREADPOOL_LIVENESS, new ThreadPoolLivenessAlarmHandler());

        eventPublisher.registerEventListener(EventSource.ALARM.name(), alarmEventListener);


        for (int i = 0; i < 100; i++){
            threadPoolExecutorWrapper.getExecutor().submit(new RunnableWrapper(threadPoolExecutorWrapper, () -> {
                log.info(UUID.randomUUID().toString());
                try {
                    Thread.sleep(new SecureRandom().nextInt(1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("中断异常", e);
                }
                if(System.currentTimeMillis() % 2 == 0){
                    log.info("result: {}",1/0);
                }
            }));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("中断异常", e);
            }
        }
    }

    private ThreadPoolExecutorProperties buildThreadPoolExecutorProperties() {
        String threadPoolId = "test2";
        ThreadPoolExecutorProperties threadPoolExecutorProperties = new ThreadPoolExecutorProperties();
        threadPoolExecutorProperties.setThreadPoolId(threadPoolId);
        threadPoolExecutorProperties.setClassName(ThreadPoolExecutor.class.getName());
        threadPoolExecutorProperties.setRejectedExecutionHandlerClassName(ThreadPoolExecutor.DiscardOldestPolicy.class.getName());
        threadPoolExecutorProperties.setWorkQueueClassName(ResizableLinkedBlockingQueue.class.getName());
        threadPoolExecutorProperties.setKeepAliveTime(60L);
        threadPoolExecutorProperties.setWorkQueueCapacity(10);
        threadPoolExecutorProperties.setAllowCoreThreadTimeOut(false);
        threadPoolExecutorProperties.setCorePoolSize(1);
        threadPoolExecutorProperties.setMaximumPoolSize(4);

        Map<String, Number> alarmThreshold = new ConcurrentHashMap<>();
        alarmThreshold.put(AlarmType.BLOCKING_QUEUE_SIZE.name(), 1000);
        alarmThreshold.put(AlarmType.THREADPOOL_LIVENESS.name(), 8);
        alarmThreshold.put(AlarmType.TASK_EXECUTION_TIMEOUT.name(), 1000);
        alarmThreshold.put(AlarmType.WAIT_TIMEOUT.name(), 10);

        threadPoolExecutorProperties.setAlarmThreshold(alarmThreshold);
        return threadPoolExecutorProperties;
    }

    private EventPublisher initEventPublisher() {
        ServiceLoader<EventPublisher> serviceLoader = ServiceLoader.load(EventPublisher.class);
        EventPublisher eventPublisher;
        if(serviceLoader.iterator().hasNext()){
            eventPublisher =  serviceLoader.iterator().next();
        }
        else {
            eventPublisher = new DefaultEventPublisher();
        }
        EventContext.init(eventPublisher);
        return eventPublisher;
    }
}
