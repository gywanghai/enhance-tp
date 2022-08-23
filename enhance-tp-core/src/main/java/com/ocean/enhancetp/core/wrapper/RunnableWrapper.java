package com.ocean.enhancetp.core.wrapper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import com.ocean.enhancetp.common.event.Event;
import com.ocean.enhancetp.common.event.EventPublisher;
import com.ocean.enhancetp.core.alarm.AlarmInfo;
import com.ocean.enhancetp.core.alarm.AlarmType;
import com.ocean.enhancetp.core.event.EventSource;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperties;
import com.ocean.enhancetp.core.vo.ExecFailRecordVO;
import com.ocean.enhancetp.core.vo.ExecTimeRecordVO;
import com.ocean.enhancetp.core.vo.WaitTimeRecordVO;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @description: 任务装饰类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/17
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class RunnableWrapper implements Runnable {

    private Runnable target;

    private ThreadPoolExecutorWrapper threadPoolExecutorWrapper;

    private EventPublisher eventPublisher;

    private Date submitDate;

    public RunnableWrapper(ThreadPoolExecutorWrapper threadPoolExecutorWrapper, Runnable target, EventPublisher eventPublisher){
        this.target = target;
        this.threadPoolExecutorWrapper = threadPoolExecutorWrapper;
        this.eventPublisher = eventPublisher;
        this.submitDate = new Date();
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    @Override
    public void run() {
        if(target != null){
            Timer.builder("threadpool.exec.time")
                    .tags(Tags.of(Tag.of("threadPoolId", threadPoolExecutorWrapper.getProperties().getThreadPoolId())))
                    .publishPercentiles(0.5,0.9,0.95,0.99)
                    .publishPercentileHistogram()
                    .serviceLevelObjectives(Duration.ofMillis(100))
                    .minimumExpectedValue(Duration.ofMillis(1))
                    .maximumExpectedValue(Duration.ofMinutes(30))
                    .register(Metrics.globalRegistry)
                    .record(() -> {
                        StopWatch stopwatch = new StopWatch();
                        stopwatch.start();
                        try {
                            checkWithQueuedTime(threadPoolExecutorWrapper);
                            target.run();
                        }catch (Exception e){
                            log.error(e.getMessage(), e);
                            // 发布任务执行失败事件
                            AlarmInfo alarmInfo = buildExecFailAlarm(threadPoolExecutorWrapper.getProperties(), e);
                            eventPublisher.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
                        }finally {
                            stopwatch.stop();
                            long mills = stopwatch.getTime(TimeUnit.MILLISECONDS);
                            ExecTimeRecordVO executionTimeVO = new ExecTimeRecordVO(target.getClass().getName(), mills);
                            // 发布执行时间统计事件
                            AlarmInfo alarmInfo = buildAlarmInfo(threadPoolExecutorWrapper.getProperties(), executionTimeVO);
                            eventPublisher.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
                        }
                    });
        }
    }

    /**
     * 检查任务等待时间
     * *
     * @param threadPoolExecutorWrapper
     */
    private void checkWithQueuedTime(ThreadPoolExecutorWrapper threadPoolExecutorWrapper) {
        long queueTime = System.currentTimeMillis() - submitDate.getTime();
        if(threadPoolExecutorWrapper != null){
            WaitTimeRecordVO waitTimeRecordVO = new WaitTimeRecordVO(target.getClass().getName(), queueTime);
            // 记录
            Timer.builder("threadpool.wait.time").tags("threadPoolId", threadPoolExecutorWrapper.getProperties().getThreadPoolId())
                    .publishPercentileHistogram()
                    .publishPercentiles(0.5,0.9,0.95,0.99)
                    .serviceLevelObjectives(Duration.ofMillis(100))
                    .minimumExpectedValue(Duration.ofMillis(1))
                    .maximumExpectedValue(Duration.ofMinutes(30))
                    .register(Metrics.globalRegistry).record(waitTimeRecordVO.getTime(), TimeUnit.MILLISECONDS);
            // 发布任务等待时间时间
            AlarmInfo alarmInfo = buildWaitTimeAlarmInfo(threadPoolExecutorWrapper.getProperties(), waitTimeRecordVO);
            eventPublisher.publishEvent(new Event<>(IdUtil.nanoId(), EventSource.ALARM.name(), alarmInfo, new Date()));
        }
    }

    private AlarmInfo buildExecFailAlarm(ThreadPoolExecutorProperties properties, Exception exception) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmType(AlarmType.TASK_FAIL);
        alarmInfo.setThreadPoolId(properties.getThreadPoolId());
        alarmInfo.setDate(new Date());

        ExecFailRecordVO execFailRecordVO = new ExecFailRecordVO(target.getClass().getName(),
                ExceptionUtil.stacktraceToString(exception));
        alarmInfo.setData(execFailRecordVO);
        return alarmInfo;
    }

    private AlarmInfo buildWaitTimeAlarmInfo(ThreadPoolExecutorProperties properties, WaitTimeRecordVO waitTimeRecordVO) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmType(AlarmType.WAIT_TIMEOUT);
        alarmInfo.setThreadPoolId(properties.getThreadPoolId());
        alarmInfo.setDate(new Date());
        alarmInfo.setData(waitTimeRecordVO);
        return alarmInfo;
    }

    private AlarmInfo buildAlarmInfo(ThreadPoolExecutorProperties properties, ExecTimeRecordVO execTimeVO) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmType(AlarmType.TASK_EXECUTION_TIMEOUT);
        alarmInfo.setThreadPoolId(properties.getThreadPoolId());
        alarmInfo.setDate(new Date());
        alarmInfo.setData(execTimeVO);
        return alarmInfo;
    }
}
