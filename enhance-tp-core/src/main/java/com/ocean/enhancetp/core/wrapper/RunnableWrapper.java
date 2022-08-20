package com.ocean.enhancetp.core.wrapper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventContext;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.vo.ExecutionFailRecordVO;
import com.ocean.enhancetp.core.vo.ExecutionTimeVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @description: 任务装饰类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/17
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class RunnableWrapper implements Runnable {

    private Runnable target;

    private TransmittableThreadLocal<ThreadPoolExecutorWrapper> context;

    public RunnableWrapper(Runnable target, TransmittableThreadLocal<ThreadPoolExecutorWrapper> context){
        this.target = target;
        this.context = context;
    }

    @Override
    public void run() {
        if(target != null){
            ThreadPoolExecutorWrapper threadPoolExecutorWrapper = context!= null ? context.get(): null;
            ThreadPoolExecutorProperties properties = threadPoolExecutorWrapper == null ? null : threadPoolExecutorWrapper.getProperties();
            long start = System.currentTimeMillis();
            try {
                target.run();
                long end = System.currentTimeMillis();
                long mills = end - start;
                if(threadPoolExecutorWrapper != null){
                    ExecutionTimeVO executionTimeVO = new ExecutionTimeVO(target.getClass().getName(), mills);
                    // 发布执行时间统计事件
                    AlarmInfo alarmInfo = buildAlarmInfo(properties, executionTimeVO);
                    EventContext.publishEvent(new Event<AlarmInfo>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
                }
            }
            catch (Exception e){
                log.error(e.getMessage(), e);
                // 发布任务执行失败事件
                if(properties != null){
                    AlarmInfo alarmInfo = new AlarmInfo();
                    alarmInfo.setAlarmType(AlarmType.TASK_FAIL);
                    alarmInfo.setApplication(properties.getApplication());
                    alarmInfo.setNamespace(properties.getNamespace());
                    alarmInfo.setThreadPoolId(properties.getThreadPoolId());
                    alarmInfo.setDate(new Date());

                    ExecutionFailRecordVO executionFailRecordVO = new ExecutionFailRecordVO(target.getClass().getName(),
                            ExceptionUtil.stacktraceToString(e));
                    alarmInfo.setData(executionFailRecordVO);

                    EventContext.publishEvent(new Event<AlarmInfo>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
                }
            }
        }
    }

    private AlarmInfo buildAlarmInfo(ThreadPoolExecutorProperties properties, ExecutionTimeVO executionTimeVO) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmType(AlarmType.TASK_EXECUTION_TIMEOUT);
        alarmInfo.setApplication(properties.getApplication());
        alarmInfo.setNamespace(properties.getNamespace());
        alarmInfo.setThreadPoolId(properties.getThreadPoolId());
        alarmInfo.setDate(new Date());
        alarmInfo.setData(executionTimeVO);
        return alarmInfo;
    }
}
