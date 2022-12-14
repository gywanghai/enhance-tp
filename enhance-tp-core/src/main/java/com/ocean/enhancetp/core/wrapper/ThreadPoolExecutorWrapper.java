package com.ocean.enhancetp.core.wrapper;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.ocean.enhancetp.core.blockingqueue.ResizableLinkedBlockingQueue;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMetrics;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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

    private ThreadPoolExecutorProperty property;

    private ThreadPoolExecutor executor;

    private ThreadPoolExecutorMetrics metrics;

    /**
     * 根据属性创建线程池装饰器
     * @param property
     */
    public ThreadPoolExecutorWrapper(ThreadPoolExecutorProperty property) {
        if(StringUtils.isEmpty(property.getClassName())){
            property.setClassName(ThreadPoolExecutor.class.getName());
        }
        this.property = property;
        buildThreadExecutor(property);

        initMetrics(property.getThreadPoolId());
    }

    /**
     * 根据线程池创建线程池包装器
     * @param threadPoolExecutor
     * @param threadPoolId
     */
    public ThreadPoolExecutorWrapper(ThreadPoolExecutor threadPoolExecutor, String threadPoolId){
        executor = threadPoolExecutor;
        property = new ThreadPoolExecutorProperty();
        property.setThreadPoolId(threadPoolId);
        property.setClassName(threadPoolExecutor.getClass().getName());

        initProperties(threadPoolExecutor);

        initMetrics(threadPoolId);

        executor.setRejectedExecutionHandler(new RejectedExecutionHandlerWrapper(executor.getRejectedExecutionHandler(), property));
    }

    private void initMetrics(String threadPoolId) {
        metrics = new ThreadPoolExecutorMetrics();
        metrics.setThreadPoolId(threadPoolId);
        metrics.setRejectedCount(new AtomicLong(0));
        metrics.setExecuteFailCount(new AtomicLong(0));
        metrics.setExecuteTimeoutCount(new AtomicLong(0));
        metrics.setWaitTimeoutCount(new AtomicLong());
        scrapeMetrics();
    }

    /**
     * 抓取线程池运行指标
     */
    public void scrapeMetrics() {
        metrics.setPoolSize(executor.getPoolSize());
        metrics.setMaximumPoolSize(executor.getMaximumPoolSize());
        metrics.setCorePoolSize(executor.getCorePoolSize());

        BlockingQueue<?> blockingQueue = executor.getQueue();
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
        Map<String, Number> map = this.getProperty().getAlarmThreshold();
        if(map == null){
            map = new ConcurrentHashMap<>();
        }
        map.put(alarmType, threshold);
    }

    /**
     * 根据线程池参数创建线程池
     * @param properties
     */
    public void buildThreadExecutor(ThreadPoolExecutorProperty properties) {
        String className = properties.getClassName();
        Class<?> clazz = ClassUtil.loadClass(className);
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
    private void buildScheduledThreadPool(ThreadPoolExecutorProperty properties) {
        String prefix = properties.getThreadPoolId() + "-";
        ThreadFactory threadFactory = new NamedThreadFactory(prefix, false);
        this.executor = new ScheduledThreadPoolExecutor(properties.getCorePoolSize(), threadFactory);
    }

    /**
     * 创建普通线程池
     * @param properties
     */
    private void buildGeneralThreadPool(ThreadPoolExecutorProperty properties){
        int corePoolSize = properties.getCorePoolSize();
        int maximumPoolSize = properties.getMaximumPoolSize();
        long keepAliveTime = properties.getKeepAliveTime();

        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = buildBlockingQueue(properties);


        String prefix = properties.getThreadPoolId() + "-";
        ThreadFactory threadFactory = new NamedThreadFactory(prefix, false);

        RejectedExecutionHandler handler = buildRejectedExecutionHandler(properties);

        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,timeUnit, workQueue, threadFactory, handler);
        this.executor.allowCoreThreadTimeOut(properties.getAllowCoreThreadTimeOut());
    }

    public RejectedExecutionHandler buildRejectedExecutionHandler(ThreadPoolExecutorProperty properties){
        String rejectedExecutionHandlerClassName = properties.getRejectedExecutionHandlerClassName();
        Class<Object> objectClass = ClassUtil.loadClass(rejectedExecutionHandlerClassName);
        RejectedExecutionHandler targetHandler = null;
        try {
            targetHandler = (RejectedExecutionHandler) objectClass.newInstance();
        } catch (Exception e) {
            log.error("创建 RejectedExecutionHandler 失败", e);
        }
        return new RejectedExecutionHandlerWrapper(targetHandler, properties);
    }

    public BlockingQueue buildBlockingQueue(ThreadPoolExecutorProperty properties)  {
        String workQueueClassName = properties.getWorkQueueClassName();
        Class<?> workQueueClass =  ClassUtil.loadClass(workQueueClassName);
        BlockingQueue workQueue = null;
        if(SynchronousQueue.class.isAssignableFrom(workQueueClass) || PriorityBlockingQueue.class.isAssignableFrom(workQueueClass) || LinkedTransferQueue.class.isAssignableFrom(workQueueClass)){
            try {
                workQueue = (BlockingQueue) workQueueClass.newInstance();
            } catch (Exception e) {
                log.error("创建阻塞队列失败", e);
            }
        }
        if(LinkedBlockingQueue.class.isAssignableFrom(workQueueClass)){
            try {
                workQueue = (BlockingQueue) workQueueClass.getConstructor(int.class).newInstance(properties.getWorkQueueCapacity());
            } catch (Exception e) {
                log.error("创建阻塞队列失败", e);
            }
        }
        return workQueue;
    }

    private void initProperties(ThreadPoolExecutor threadPoolExecutor) {
        property.setCorePoolSize(executor.getCorePoolSize());
        property.setAllowCoreThreadTimeOut(threadPoolExecutor.allowsCoreThreadTimeOut());
        property.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        property.setKeepAliveTime(threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        property.setWorkQueueClassName(threadPoolExecutor.getQueue().getClass().getName());

        initWorkQueueProperties(threadPoolExecutor);

        initRejectedExecutionHandlerProperties(threadPoolExecutor);
    }

    private void initRejectedExecutionHandlerProperties(ThreadPoolExecutor threadPoolExecutor) {
        RejectedExecutionHandler rejectedExecutionHandler = threadPoolExecutor.getRejectedExecutionHandler();
        if(rejectedExecutionHandler instanceof RejectedExecutionHandlerWrapper){
            property.setRejectedExecutionHandlerClassName(((RejectedExecutionHandlerWrapper) rejectedExecutionHandler).getRejectedExecutionHandler().getClass().getName());
        }
        else {
            property.setRejectedExecutionHandlerClassName(rejectedExecutionHandler.getClass().getName());
        }
    }

    private void initWorkQueueProperties(ThreadPoolExecutor threadPoolExecutor) {
        if(threadPoolExecutor.getQueue() instanceof SynchronousQueue){
            property.setWorkQueueCapacity(0);
        }
        if(threadPoolExecutor.getQueue() instanceof ArrayBlockingQueue){
            Object[] items = (Object[]) ReflectUtil.getFieldValue(threadPoolExecutor.getQueue(),"items");
            property.setWorkQueueCapacity(items.length);
        }
        if(threadPoolExecutor.getQueue() instanceof LinkedBlockingQueue){
            Integer workQueueCapacity = (Integer) ReflectUtil.getFieldValue(threadPoolExecutor.getQueue(), "capacity");
            property.setWorkQueueCapacity(workQueueCapacity);
        }
        if(threadPoolExecutor.getQueue() instanceof DelayQueue){
            property.setWorkQueueCapacity(MAX_VALUE - 8);
        }
        if(threadPoolExecutor.getQueue() instanceof PriorityBlockingQueue){
            property.setWorkQueueCapacity(MAX_VALUE - 8);
        }
        if(threadPoolExecutor.getQueue() instanceof LinkedBlockingDeque){
            Integer workQueueCapacity = (Integer) ReflectUtil.getFieldValue(threadPoolExecutor.getQueue(), "capacity");
            property.setWorkQueueCapacity(workQueueCapacity);
        }
        if(threadPoolExecutor.getQueue() instanceof LinkedTransferQueue){
            property.setWorkQueueCapacity(MAX_VALUE);
        }
    }

    /**
     * 更新线程池参数
     * @param threadPoolExecutorProperty
     */
    public void update(ThreadPoolExecutorProperty threadPoolExecutorProperty){
        if(threadPoolExecutorProperty.getMaximumPoolSize() < threadPoolExecutorProperty.getCorePoolSize()){
            log.error("最大线程数不能小于核心线程数");
            return;
        }
        if(threadPoolExecutorProperty.getCorePoolSize() != null){
            executor.setCorePoolSize(threadPoolExecutorProperty.getCorePoolSize());
        }
        if(threadPoolExecutorProperty.getMaximumPoolSize() != null){
            executor.setMaximumPoolSize(threadPoolExecutorProperty.getMaximumPoolSize());
        }
        if(threadPoolExecutorProperty.getKeepAliveTime() != null){
            executor.setKeepAliveTime(threadPoolExecutorProperty.getKeepAliveTime(), TimeUnit.SECONDS);
        }
        String prefix = threadPoolExecutorProperty.getThreadPoolId() + "-";
        executor.setThreadFactory(new NamedThreadFactory(prefix, false));
        executor.allowCoreThreadTimeOut(threadPoolExecutorProperty.getAllowCoreThreadTimeOut());
        updateRejectedExecutionHandler(threadPoolExecutorProperty);
        if(executor.getQueue() instanceof ResizableLinkedBlockingQueue){
            ((ResizableLinkedBlockingQueue<Runnable>) executor.getQueue()).setCapacity(threadPoolExecutorProperty.getWorkQueueCapacity());
        }
        this.property = threadPoolExecutorProperty;
    }

    private void updateRejectedExecutionHandler(ThreadPoolExecutorProperty threadPoolExecutorProperty) {
        if(threadPoolExecutorProperty.getRejectedExecutionHandlerClassName() != null){
            String className = threadPoolExecutorProperty.getRejectedExecutionHandlerClassName();
            Class<?> rejectedExecutionHandlerClass = ClassUtil.loadClass(className);
            try {
                RejectedExecutionHandler handler = (RejectedExecutionHandler) rejectedExecutionHandlerClass.newInstance();
                executor.setRejectedExecutionHandler(new RejectedExecutionHandlerWrapper(handler, threadPoolExecutorProperty));
            } catch (Exception e) {
                log.error("设置 RejectedExecutionHandler 失败", e);
            }
        }
    }

    public void destory(){
        if(null != property){
            property = null;
        }
        if(executor != null){
            executor.shutdown();
            log.error("线程池[{}]关闭", property);
        }
    }

    public void increaseWaitTimeoutCount() {
        this.metrics.getWaitTimeoutCount().incrementAndGet();
    }

    public void increaseFailCount() {
        this.metrics.getExecuteFailCount().incrementAndGet();
    }

    public void increaseExecTimeoutCount() {
        this.metrics.getExecuteTimeoutCount().incrementAndGet();
    }

    public void increaseRejectedCount() {
        this.metrics.getRejectedCount().incrementAndGet();
    }
}
