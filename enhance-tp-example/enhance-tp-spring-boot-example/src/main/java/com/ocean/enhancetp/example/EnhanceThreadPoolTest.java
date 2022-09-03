package com.ocean.enhancetp.example;

import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import com.ocean.enhancetp.core.wrapper.RunnableWrapper;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
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
@Component
public class EnhanceThreadPoolTest implements ApplicationContextAware,ApplicationRunner {

    private ApplicationContext applicationContext;

    @Autowired
    private EventPublisher eventPublisher;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        afterSingletonsInstantiated();
    }

    public void afterSingletonsInstantiated() {
        ThreadPoolExecutor threadPoolExecutor = applicationContext.getBean("threadPoolExecutor", ThreadPoolExecutor.class);
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = applicationContext.getBean("threadPoolExecutorWrapper", ThreadPoolExecutorWrapper.class);
        ThreadPoolExecutorProperty threadPoolExecutorProperty = threadPoolExecutorWrapper.getProperty();

        Map<String, Number> alarmThreshold = new ConcurrentHashMap<>();
        alarmThreshold.put(AlarmType.BLOCKING_QUEUE_SIZE.name(), 1000);
        alarmThreshold.put(AlarmType.THREADPOOL_LIVENESS.name(), 8);
        alarmThreshold.put(AlarmType.TASK_EXECUTION_TIMEOUT.name(), 10);
        alarmThreshold.put(AlarmType.WAIT_TIMEOUT.name(), 10);

        threadPoolExecutorProperty.setAlarmThreshold(alarmThreshold);
//        for (int i = 0; i < 1000; i++){
//            threadPoolExecutor.submit(new RunnableWrapper(threadPoolExecutorWrapper, () -> {
//                log.info(UUID.randomUUID().toString());
//                try {
//                    Thread.sleep(new SecureRandom().nextInt(100));
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    log.error("中断异常", e);
//                }
//                if(System.currentTimeMillis() % 2 == 0){
//                    log.info("result: {}",1/0);
//                }
//            }, eventPublisher));
//        }
    }
}
