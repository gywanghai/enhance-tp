package com.ocean.enhancetp.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventContext;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.vo.UpdateRecordVO;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

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
        alarmInfo.setDate(new Date());
        alarmInfo.setData(new UpdateRecordVO(executorWrapper.getProperties(), threadPoolExecutorProperties));
        alarmInfo.setAlarmType(AlarmType.CONFIG_UPDATE);

        EventContext.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo ,new Date()));
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
    public Collection<ThreadPoolExecutorWrapper> getAllThreadPoolExecutorWrapper() {
        return threadPoolExecutorWrapperMap.values();
    }

    public void destroy(){
        log.info("关闭所有线程池");
        threadPoolExecutorWrapperMap.entrySet().stream().forEach(wrapperEntry -> wrapperEntry.getValue().destory());
    }
}
