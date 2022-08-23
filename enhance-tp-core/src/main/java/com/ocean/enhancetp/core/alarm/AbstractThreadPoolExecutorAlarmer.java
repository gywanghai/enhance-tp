package com.ocean.enhancetp.core.alarm;

import cn.hutool.core.util.IdUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMetrics;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;

import java.util.Date;
import java.util.List;

/**
 * @description: 抽象线程池告警器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public abstract class AbstractThreadPoolExecutorAlarmer implements ThreadPoolExecutorAlarmer {

    private EventPublisher eventPublisher;

    protected AbstractThreadPoolExecutorAlarmer(EventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void alarm(ThreadPoolExecutorMetrics threadPoolExecutorMetrics, ThreadPoolExecutorProperties properties) {
        List<AlarmInfo> alarmInfoList = checkMetrics(threadPoolExecutorMetrics, properties);
        if(alarmInfoList.isEmpty()){
            return;
        }
        alarmInfoList.stream().forEach(this::doAlarm);
    }

    public void doAlarm(AlarmInfo alarmInfo){
        eventPublisher.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
    }

    /**
     * 检查是否报警
     * @param threadPoolExecutorMetrics 线程池状态指标
     * @return
     */
    public abstract List<AlarmInfo> checkMetrics(ThreadPoolExecutorMetrics threadPoolExecutorMetrics, ThreadPoolExecutorProperties properties);
}
