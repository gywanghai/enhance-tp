package com.ocean.enhancetp.core.refresher;

import cn.hutool.core.util.IdUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.vo.UpdateRecordVO;

import java.util.Date;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/23
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class ThreadPoolExecutorRefresher {

    private ThreadPoolExecutorService threadPoolExecutorService;

    private EventPublisher eventPublisher;

    public ThreadPoolExecutorRefresher(ThreadPoolExecutorService threadPoolExecutorService, EventPublisher eventPublisher){
        this.threadPoolExecutorService = threadPoolExecutorService;
        this.eventPublisher = eventPublisher;
    }

    public void update(ThreadPoolExecutorProperties oldProperties, ThreadPoolExecutorProperties newProperties){
        threadPoolExecutorService.update(oldProperties.getThreadPoolId(),newProperties);

        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setThreadPoolId(oldProperties.getThreadPoolId());
        alarmInfo.setDate(new Date());
        alarmInfo.setData(new UpdateRecordVO(oldProperties, newProperties));
        alarmInfo.setAlarmType(AlarmType.CONFIG_UPDATE);

        eventPublisher.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo ,new Date()));
    }
}
