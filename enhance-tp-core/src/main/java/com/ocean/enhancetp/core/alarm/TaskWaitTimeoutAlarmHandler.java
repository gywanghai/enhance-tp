package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.vo.WaitTimeRecordVO;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/22
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class TaskWaitTimeoutAlarmHandler implements AlarmEventHandler{

    @Override
    public void handler(AlarmInfo alarmInfo, ThreadPoolExecutorWrapper threadPoolExecutorWrapper) {
        ThreadPoolExecutorProperties properties = threadPoolExecutorWrapper.getProperties();
        Map<String,Number> thresholdMap =  properties.getAlarmThreshold();
        if(!thresholdMap.containsKey(AlarmType.WAIT_TIMEOUT.name())){
            return;
        }
        Integer threshold = (Integer) thresholdMap.get(AlarmType.WAIT_TIMEOUT.name());
        WaitTimeRecordVO waitTimeRecordVO = (WaitTimeRecordVO) alarmInfo.getData();
        if(waitTimeRecordVO.getTime() > threshold){
            log.info("任务等待超时告警: {}", alarmInfo);
            threadPoolExecutorWrapper.increaseWaitTimeoutCount();
        }
    }

}
