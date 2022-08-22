package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.core.vo.ExecTimeRecordVO;
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
    public void handler(AlarmInfo alarmInfo, ThreadPoolExecutorWrapper threadPoolExecutorWrapper) {
        Integer alarmThreshold = (Integer) threadPoolExecutorWrapper.getProperties().getAlarmThreshold().get(AlarmType.TASK_EXECUTION_TIMEOUT.name());
        ExecTimeRecordVO execTimeRecordVO = (ExecTimeRecordVO) alarmInfo.getData();
        if(alarmThreshold < execTimeRecordVO.getTime()){
            log.info("任务执行超时告警: {}", alarmInfo);
            threadPoolExecutorWrapper.increaseExecTimeoutCount();
        }
    }
}
