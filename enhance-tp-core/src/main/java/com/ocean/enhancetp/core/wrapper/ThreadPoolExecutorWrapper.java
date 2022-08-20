package com.ocean.enhancetp.core.wrapper;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.ocean.enhancetp.core.blockingqueue.ResizableLinkedBlockingQueue;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMetrics;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.vo.ExecutionTimeVO;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Integer.MAX_VALUE;

/**
 *
 * @description: 线程池装饰类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
@Slf4j
public class ThreadPoolExecutorWrapper {

    private ThreadPoolExecutorProperties properties;

    private ThreadPoolExecutor executor;

    private ThreadPoolExecutorMetrics metrics;

    /**
     * 根据属性创建线程池装饰器
     * @param properties
     */
    public ThreadPoolExecutorWrapper(ThreadPoolExecutorProperties properties) {
        if(StringUtils.isEmpty(properties.getClassName())){
            properties.setClassName(ThreadPoolExecutor.class.getName());
        }
        this.properties = properties;
        buildThreadExecutor(properties);

        metrics = new ThreadPoolExecutorMetrics();
        initMetrics(properties.getNamespace(), properties.getApplication(), properties.getThreadPoolId());
    }

    /**
     * 根据线程池创建线程池包装器
     * @param threadPoolExecutor
     * @param namespace
     * @param application
     * @param threadPoolId
     */
    public ThreadPoolExecutorWrapper(ThreadPoolExecutor threadPoolExecutor, String namespace, String application, String threadPoolId){
        executor = threadPoolExecutor;
        properties = new ThreadPoolExecutorProperties();
        properties.setNamespace(namespace);
        properties.setThreadPoolId(threadPoolId);
        properties.setApplication(application);
        properties.setClassName(threadPoolExecutor.getClass().getName());

        initProperties(threadPoolExecutor);

        metrics = new ThreadPoolExecutorMetrics();
        initMetrics(namespace, application, threadPoolId);

        executor.setRejectedExecutionHandler(new RejectedExecutionHandlerWrapper(executor.getRejectedExecutionHandler(), properties));
    }

    private void initMetrics(String namespace, String application, String threadPoolId) {
        metrics.setNamespace(namespace);
        metrics.setApplication(application);
        metrics.setThreadPoolId(threadPoolId);
        metrics.setRejectedCount(new AtomicLong(0));
        metrics.setExecuteFailCount(new AtomicLong(0));
        metrics.setExecuteTimeoutCount(new AtomicLong(0));
        scrapeMetrics();
    }

    /**
     * 抓取线程池运行指标
     */
    public void scrapeMetrics() {
        metrics.setPoolSize(executor.getPoolSize());
        metrics.setMaximumPoolSize(executor.getMaximumPoolSize());
        metrics.setCorePoolSize(executor.getCorePoolSize());

        BlockingQueue blockingQueue = executor.getQueue();
        metrics.setWorkQueueSize(blockingQueue.size());

        metrics.setWorkQueueCapacity(blockingQueue.remainingCapacity() + blockingQueue.size());

        metrics.setLargestPoolSize(executor.getLargestPoolSize());
        metrics.setActiveCount(executor.getActiveCount());
        metrics.setTaskCount(executor.getTaskCount());
        metrics.setCompletedTaskCount(executor.getCompletedTaskCount());
    }

    /**
     * 设置报警阈值
     * @param alarmType
     * @param threshold
     */
    public void setAlarmThreshold(String alarmType, Number threshold){
        Map<String, Number> map = this.getProperties().getAlarmThreshold();
        if(map == null){
            map = new ConcurrentHashMap<>();
        }
        map.put(alarmType, threshold);
    }

    /**
     * 根据线程池参数创建线程池
     * @param properties
     */
    public void buildThreadExecutor(ThreadPoolExecutorProperties properties) {
        String className = properties.getClassName();
        Class clazz = ClassUtil.loadClass(className);
        if(clazz == ThreadPoolExecutor.class){
            buildGeneralThreadPool(properties);
        }
        if(clazz == ScheduledThreadPoolExecutor.class){
            buildScheduledThreadPool(properties);
        }
    }

    /**
     * 创建定时任务线程池
     * @param properties
     */
    private void buildScheduledThreadPool(ThreadPoolExecutorProperties properties) {
        String prefix = properties.getNamespace() + "-" + properties.getApplication() + "-" + properties.getThreadPoolId() + "-";
        ThreadFactory threadFactory = new NamedThreadFactory(prefix, false);
        this.executor = new ScheduledThreadPoolExecutor(properties.getCorePoolSize(), threadFactory);
    }

    /**
     * 创建普通线程池
     * @param properties
     */
    private void buildGeneralThreadPool(ThreadPoolExecutorProperties properties){
        int corePoolSize = properties.getCorePoolSize();
        int maximumPoolSize = properties.getMaximumPoolSize();
        long keepAliveTime = properties.getKeepAliveTime();

        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue workQueue = buildBlockingQueue(properties);


        String prefix = properties.getNamespace() + "-" + properties.getApplication() + "-" + properties.getThreadPoolId() + "-";
        ThreadFactory threadFactory = new NamedThreadFactory(prefix, false);

        RejectedExecutionHandler handler = buildRejectedExecutionHandler(properties);

        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,timeUnit, workQueue, threadFactory, handler);
    }

    public RejectedExecutionHandler buildRejectedExecutionHandler(ThreadPoolExecutorProperties properties){
        String rejectedExecutionHandlerClassName = properties.getRejectedExecutionHandlerClassName();
        Class<Object> objectClass = ClassUtil.loadClass(rejectedExecutionHandlerClassName);
        RejectedExecutionHandler targetHandler = null;
        try {
            targetHandler = (RejectedExecutionHandler) objectClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return new RejectedExecutionHandlerWrapper(targetHandler, properties);
    }

    public BlockingQueue buildBlockingQueue(ThreadPoolExecutorProperties properties)  {
        String workQueueClassName = properties.getWorkQueueClassName();
        Class workQueueClass =  ClassUtil.loadClass(workQueueClassName);
        BlockingQueue workQueue = null;
        if(SynchronousQueue.class.isAssignableFrom(workQueueClass) || PriorityBlockingQueue.class.isAssignableFrom(workQueueClass) || LinkedTransferQueue.class.isAssignableFrom(workQueueClass)){
            try {
                workQueue = (BlockingQueue) workQueueClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if(LinkedBlockingQueue.class.isAssignableFrom(workQueueClass)){
            try {
                workQueue = (BlockingQueue) workQueueClass.getConstructor(int.class).newInstance(properties.getWorkQueueCapacity());
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return workQueue;
    }

    private void initProperties(ThreadPoolExecutor threadPoolExecutor) {
        properties.setCorePoolSize(executor.getCorePoolSize());
        properties.setAllowCoreThreadTimeOut(threadPoolExecutor.allowsCoreThreadTimeOut());
        properties.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        properties.setKeepAliveTime(threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        properties.setWorkQueueClassName(threadPoolExecutor.getQueue().getClass().getName());

        initWorkQueueProperties(threadPoolExecutor);

        initRejectedExecutionHandlerProperties(threadPoolExecutor);
    }

    private void initRejectedExecutionHandlerProperties(ThreadPoolExecutor threadPoolExecutor) {
        RejectedExecutionHandler rejectedExecutionHandler = threadPoolExecutor.getRejectedExecutionHandler();
        if(rejectedExecutionHandler instanceof RejectedExecutionHandlerWrapper){
            properties.setRejectedExecutionHandlerClassName(((RejectedExecutionHandlerWrapper) rejectedExecutionHandler).getRejectedExecutionHandler().getClass().getName());
        }
        else {
            properties.setRejectedExecutionHandlerClassName(rejectedExecutionHandler.getClass().getName());
        }
    }

    private void initWorkQueueProperties(ThreadPoolExecutor threadPoolExecutor) {
        if(threadPoolExecutor.getQueue() instanceof SynchronousQueue){
            properties.setWorkQueueCapacity(0);
        }
        if(threadPoolExecutor.getQueue() instanceof ArrayBlockingQueue){
            Object[] items = (Object[]) ReflectUtil.getFieldValue(threadPoolExecutor.getQueue(),"items");
            properties.setWorkQueueCapacity(items.length);
        }
        if(threadPoolExecutor.getQueue() instanceof LinkedBlockingQueue){
            Integer workQueueCapacity = (Integer) ReflectUtil.getFieldValue(threadPoolExecutor.getQueue(), "capacity");
            properties.setWorkQueueCapacity(workQueueCapacity);
        }
        if(threadPoolExecutor.getQueue() instanceof DelayQueue){
            properties.setWorkQueueCapacity(MAX_VALUE - 8);
        }
        if(threadPoolExecutor.getQueue() instanceof PriorityBlockingQueue){
            properties.setWorkQueueCapacity(MAX_VALUE - 8);
        }
        if(threadPoolExecutor.getQueue() instanceof LinkedBlockingDeque){
            Integer workQueueCapacity = (Integer) ReflectUtil.getFieldValue(threadPoolExecutor.getQueue(), "capacity");
            properties.setWorkQueueCapacity(workQueueCapacity);
        }
        if(threadPoolExecutor.getQueue() instanceof LinkedTransferQueue){
            properties.setWorkQueueCapacity(MAX_VALUE);
        }
    }

    /**
     * 更新线程池参数
     * @param threadPoolExecutorProperties
     */
    public void update(ThreadPoolExecutorProperties threadPoolExecutorProperties){
        if(threadPoolExecutorProperties.getMaximumPoolSize() < threadPoolExecutorProperties.getCorePoolSize()){
            log.error("最大线程数不能小于核心线程数");
            return;
        }
        if(threadPoolExecutorProperties.getCorePoolSize() != null){
            executor.setCorePoolSize(threadPoolExecutorProperties.getCorePoolSize());
        }
        if(threadPoolExecutorProperties.getMaximumPoolSize() != null){
            executor.setMaximumPoolSize(threadPoolExecutorProperties.getMaximumPoolSize());
        }
        if(threadPoolExecutorProperties.getKeepAliveTime() != null){
            executor.setKeepAliveTime(threadPoolExecutorProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        }
        String prefix = threadPoolExecutorProperties.getNamespace() + "-" +
                threadPoolExecutorProperties.getApplication() + "-" +
                threadPoolExecutorProperties.getThreadPoolId() + "-";
        executor.setThreadFactory(new NamedThreadFactory(prefix, false));
        executor.allowCoreThreadTimeOut(threadPoolExecutorProperties.getAllowCoreThreadTimeOut());
        updateRejectedExecutionHandler(threadPoolExecutorProperties);
        if(executor.getQueue() instanceof ResizableLinkedBlockingQueue){
            ((ResizableLinkedBlockingQueue<Runnable>) executor.getQueue()).setCapacity(threadPoolExecutorProperties.getWorkQueueCapacity());
        }
        this.properties = threadPoolExecutorProperties;
    }

    private void updateRejectedExecutionHandler(ThreadPoolExecutorProperties threadPoolExecutorProperties) {
        if(threadPoolExecutorProperties.getRejectedExecutionHandlerClassName() != null){
            String className = threadPoolExecutorProperties.getRejectedExecutionHandlerClassName();
            Class<?> rejectedExecutionHandlerClass = ClassUtil.loadClass(className);
            try {
                RejectedExecutionHandler handler = (RejectedExecutionHandler) rejectedExecutionHandlerClass.newInstance();
                executor.setRejectedExecutionHandler(new RejectedExecutionHandlerWrapper(handler, threadPoolExecutorProperties));
            } catch (Exception e) {
                log.error("设置 RejectedExecutionHandler 失败", e);
            }
        }
    }

    public void destory(){
        if(null != properties){
            properties = null;
        }
        if(executor != null){
            executor.shutdown();
            log.error("线程池[{}]关闭", properties);
        }
    }

    /**
     * 统计任务执行时间
     * @param executionTimeVO
     */
    public void time(ExecutionTimeVO executionTimeVO) {

    }
}
