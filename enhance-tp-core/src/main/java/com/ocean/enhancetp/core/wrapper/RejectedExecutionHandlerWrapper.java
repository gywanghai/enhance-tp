package com.ocean.enhancetp.core.wrapper;

import cn.hutool.core.util.IdUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventContext;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.vo.RejectedExecutionRecordVO;

import java.util.Date;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description: 拒绝策略装饰类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class RejectedExecutionHandlerWrapper implements RejectedExecutionHandler {

    private RejectedExecutionHandler rejectedExecutionHandler;

    private String threadPoolId;

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public RejectedExecutionHandlerWrapper(RejectedExecutionHandler rejectedExecutionHandler, ThreadPoolExecutorProperties threadPoolExecutorProperties ){
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.threadPoolId = threadPoolExecutorProperties.getThreadPoolId();
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 发布任务被拒绝事件
        RejectedExecutionRecordVO rejectedExecutionRecord = new RejectedExecutionRecordVO();
        rejectedExecutionRecord.setRunnableClassName(r.getClass().getName());
        if(r instanceof RunnableWrapper){
            rejectedExecutionRecord.setRunnableClassName(((RunnableWrapper)r).getClass().getName());
        }

        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setThreadPoolId(threadPoolId);
        alarmInfo.setAlarmType(AlarmType.TASK_REJECTED);
        alarmInfo.setDate(new Date());
        alarmInfo.setData(rejectedExecutionRecord);
        EventContext.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
        this.rejectedExecutionHandler.rejectedExecution(r, executor);
    }
}
