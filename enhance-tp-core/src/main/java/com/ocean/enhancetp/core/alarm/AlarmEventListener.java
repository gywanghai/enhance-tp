package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventListener;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/18
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class AlarmEventListener implements EventListener<AlarmInfo> {

    private ThreadPoolExecutorService threadPoolExecutorService;

    private Map<AlarmType, AlarmEventHandler> alarmEventHandlerFactoryMap = new ConcurrentHashMap<>();

    public AlarmEventListener(ThreadPoolExecutorService threadPoolExecutorService){
        this.threadPoolExecutorService = threadPoolExecutorService;
    }

    @Override
    public void onMessage(Event<AlarmInfo> event) {
        AlarmInfo alarmInfo = event.getData();
        if(null != alarmInfo){
            AlarmEventHandler alarmEventHandler = alarmEventHandlerFactoryMap.get(alarmInfo.getAlarmType());
            if(null != alarmEventHandler){
                alarmEventHandler.handler(alarmInfo, threadPoolExecutorService.getThreadPoolExecutorWrapper(alarmInfo.getThreadPoolId()));
            }
        }
    }

    public void registerHandler(AlarmType alarmType, AlarmEventHandler alarmEventHandler) {
        alarmEventHandlerFactoryMap.put(alarmType, alarmEventHandler);
    }
}
