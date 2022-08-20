package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.service.ThreadPoolExecutorService;
import com.ocean.enhancetp.core.vo.ExecutionTimeVO;
import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/19
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class TaskExecutionTimeoutAlarmHandler implements AlarmEventHandler{

    @Override
    public void handler(AlarmInfo alarmInfo, ThreadPoolExecutorService threadPoolExecutorService) {
        ThreadPoolExecutorWrapper wrapper = threadPoolExecutorService.getThreadPoolExecutorWrapper(alarmInfo.getThreadPoolId());
        ThreadPoolExecutorProperties threadPoolExecutorProperties = wrapper.getProperties();
        if(!threadPoolExecutorProperties.getAlarmThreshold().containsKey(AlarmType.TASK_EXECUTION_TIMEOUT.name())){
            return;
        }
        Integer alarmThreshold = (Integer) threadPoolExecutorProperties.getAlarmThreshold().get(AlarmType.TASK_EXECUTION_TIMEOUT.name());
        ExecutionTimeVO executionTimeVO = (ExecutionTimeVO) alarmInfo.getData();
        if(alarmThreshold < executionTimeVO.getCostTime()){
            log.info("任务执行超时告警: {}", alarmInfo);
            threadPoolExecutorService.increaseExecTimeoutCount(alarmInfo.getThreadPoolId());
        }
    }
}
