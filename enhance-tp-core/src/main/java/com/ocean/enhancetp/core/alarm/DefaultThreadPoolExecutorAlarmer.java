package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.core.monitor.ThreadPoolExecutorMetrics;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description: 默认线程告警器实现类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class DefaultThreadPoolExecutorAlarmer extends AbstractThreadPoolExecutorAlarmer {
    @Override
    public List<AlarmInfo> checkMetrics(ThreadPoolExecutorMetrics threadPoolExecutorMetrics, ThreadPoolExecutorProperties properties) {
        List<AlarmInfo> list = new ArrayList<>();
        int activeCount = threadPoolExecutorMetrics.getActiveCount();
        int queueSize = threadPoolExecutorMetrics.getWorkQueueSize();

        // 线程池活跃度
        Map<String, Number> alarmThreshold = properties.getAlarmThreshold();
        Integer livenessThresold = (Integer) alarmThreshold.get(AlarmType.THREADPOOL_LIVENESS.name());
        if(null != livenessThresold && activeCount >= livenessThresold){
            AlarmInfo alarmInfo = new AlarmInfo();
            alarmInfo.setAlarmType(AlarmType.THREADPOOL_LIVENESS);
            alarmInfo.setDate(new Date());
            alarmInfo.setThreadPoolId(properties.getThreadPoolId());
            alarmInfo.setData(threadPoolExecutorMetrics);
            list.add(alarmInfo);
        }

        /**
         * 队列容量
         */
        Integer queueSizeAlarmThreshold = (Integer) alarmThreshold.get(AlarmType.BLOCKING_QUEUE_SIZE.name());
        if(null != queueSizeAlarmThreshold && queueSize >= queueSizeAlarmThreshold){
            AlarmInfo alarmInfo = new AlarmInfo();
            alarmInfo.setAlarmType(AlarmType.BLOCKING_QUEUE_SIZE);
            alarmInfo.setDate(new Date());
            alarmInfo.setThreadPoolId(properties.getThreadPoolId());
            alarmInfo.setData(threadPoolExecutorMetrics);
            list.add(alarmInfo);
        }

        return list;
    }
}
