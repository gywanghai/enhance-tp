package com.ocean.enhancetp.autoconfig.refresher;

import cn.hutool.core.util.IdUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.config.ConfigChangeRecord;
import com.ocean.enhancetp.config.ConfigUpdateEvent;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.vo.UpdateRecordVO;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

import java.util.Date;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/23
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ThreadPoolExecutorRefresher implements ApplicationListener<ConfigUpdateEvent> {

    private ThreadPoolExecutorService threadPoolExecutorService;

    private EventPublisher eventPublisher;

    public ThreadPoolExecutorRefresher(ThreadPoolExecutorService threadPoolExecutorService, EventPublisher eventPublisher){
        this.threadPoolExecutorService = threadPoolExecutorService;
        this.eventPublisher = eventPublisher;
    }

    public void update(String threadPoolId, ThreadPoolExecutorProperty newProperty){
        ThreadPoolExecutorWrapper threadPoolExecutorWrapper = threadPoolExecutorService.getThreadPoolExecutorWrapper(threadPoolId);
        if(threadPoolExecutorWrapper == null){
            throw new IllegalStateException("ThreadPoolExecutorWrapper[threadPoolId=" + threadPoolId + "] not exist");
        }
        ThreadPoolExecutorProperty oldProperty = threadPoolExecutorWrapper.getProperty();
        threadPoolExecutorService.update(threadPoolId, newProperty);

        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setThreadPoolId(threadPoolId);
        alarmInfo.setDate(new Date());
        alarmInfo.setData(new UpdateRecordVO(oldProperty, newProperty));
        alarmInfo.setAlarmType(AlarmType.CONFIG_UPDATE);

        eventPublisher.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo ,new Date()));
    }

    @Override
    public void onApplicationEvent(ConfigUpdateEvent event) {
        log.info("接收到配置变更事件 : {}", event.getSource());
        ConfigChangeRecord<ThreadPoolExecutorProperty> changeRecord = (ConfigChangeRecord<ThreadPoolExecutorProperty>) event.getSource();
        String dataId = changeRecord.getDataId();
        if(changeRecord.getNewValue() != null){
            this.update(dataId, changeRecord.getNewValue());
        }
    }
}
