package com.ocean.enhancetp.core.alarm;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventListener;
import com.ocean.enhancetp.common.spi.SpiOrder;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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
        initAlarmEventHandler();
    }

    private void initAlarmEventHandler() {
        List<AlarmEventHandlerFactory> list = new ArrayList<>();
        ServiceLoader<AlarmEventHandlerFactory> serviceLoader = ServiceLoaderUtil.load(AlarmEventHandlerFactory.class);
        Iterator<AlarmEventHandlerFactory> iterator = serviceLoader.iterator();
        while (iterator.hasNext()){
            list.add(iterator.next());
        }
        if(list.isEmpty()){
            log.error("未配置 AlarmEventHandler ");
            return;
        }
        // 按 SpiOrder 排序，对于每一种 AlarmType来说，SpiOrder 序号大的生效
        Collections.sort(list, (o1, o2) -> {
            SpiOrder spiOrder1 = o1.getClass().getAnnotation(SpiOrder.class);
            SpiOrder spiOrder2 = o2.getClass().getAnnotation(SpiOrder.class);
            int order1 = spiOrder1 != null? spiOrder1.order() : 0;
            int order2 = spiOrder2 != null? spiOrder2.order() : 0;
            return order1 - order2;
        });
        list.stream().forEach(alarmEventHandlerFactory -> this.alarmEventHandlerFactoryMap.put(alarmEventHandlerFactory.alarmType(),
                alarmEventHandlerFactory.getAlarmEventHandler()));
    }

    @Override
    public void onMessage(Event<AlarmInfo> event) {
        AlarmInfo alarmInfo = event.getData();
        if(null != alarmInfo){
            AlarmEventHandler alarmEventHandler = alarmEventHandlerFactoryMap.get(alarmInfo.getAlarmType());
            if(null != alarmEventHandler){
                alarmEventHandler.handler(alarmInfo, threadPoolExecutorService);
            }
        }
    }
}
