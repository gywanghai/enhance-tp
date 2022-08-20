package com.ocean.enhancetp.example;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.RejectPolicy;
import cn.hutool.json.JSONUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.blockingqueue.ResizableLinkedBlockingQueue;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.service.impl.ThreadPoolExecutorServiceImpl;
import com.ocean.enhancetp.core.wrapper.RunnableWrapper;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/17
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ThreadPoolExecutorWrapperTest {

    public static void main(String[] args) throws InterruptedException {
//        testExistsThreadPool();
        testCreateThreadPoolFromProperties();
    }

    private static void testExistsThreadPool() {
        String namespace = "dev";
        String application = "test";
        String threadPoolId = "test1";
        // 创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 60,
                TimeUnit.SECONDS,
                new ResizableLinkedBlockingQueue<>(65535), new NamedThreadFactory("test-pool-", false));
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = new ThreadPoolExecutorWrapper(threadPoolExecutor, namespace, application, threadPoolId);

        threadPoolExecutorWrapper.setAlarmThreshold(AlarmType.BLOCKING_QUEUE_SIZE.name(), 1);
        threadPoolExecutorWrapper.setAlarmThreshold(AlarmType.THREADPOOL_LIVENESS.name(), 1);
        threadPoolExecutorWrapper.setAlarmThreshold(AlarmType.TASK_EXECUTION_TIMEOUT.name(), 90);

        ThreadPoolExecutorService threadPoolExecutorService = new ThreadPoolExecutorServiceImpl();
        threadPoolExecutorService.registerThreadPoolExecutorWrapper(threadPoolExecutorWrapper);
        threadPoolExecutorService.startMonitor();
        ThreadPoolExecutorProperties newProperties = threadPoolExecutorWrapper.getProperties();
        newProperties.setCorePoolSize(4);
        newProperties.setMaximumPoolSize(4);
        newProperties.setAllowCoreThreadTimeOut(false);
        newProperties.setWorkQueueCapacity(Integer.MAX_VALUE);

        threadPoolExecutorService.update(threadPoolId, newProperties);

        TransmittableThreadLocal<ThreadPoolExecutorWrapper> context = new TransmittableThreadLocal<>();
        context.set(threadPoolExecutorWrapper);
        for (int i = 0; i < 1000; i++){
            threadPoolExecutor.submit(new RunnableWrapper(new Runnable() {
                @Override
                public void run() {
                    System.out.println(context.get());
                }
            },context));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void testCreateThreadPoolFromProperties() {
        String namespace = "dev";
        String application = "test";
        String threadPoolId = "test2";
        ThreadPoolExecutorProperties threadPoolExecutorProperties = new ThreadPoolExecutorProperties();
        threadPoolExecutorProperties.setApplication(application);
        threadPoolExecutorProperties.setNamespace(namespace);
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
        threadPoolExecutorService.startMonitor();

        TransmittableThreadLocal<ThreadPoolExecutorWrapper> context = new TransmittableThreadLocal<>();
        context.set(threadPoolExecutorWrapper);

        for (int i = 0; i < 1000; i++){
            threadPoolExecutorWrapper.getExecutor().submit(new RunnableWrapper(new Runnable() {
                @Override
                public void run() {
                    System.out.println(context.get());
                }
            },context));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
