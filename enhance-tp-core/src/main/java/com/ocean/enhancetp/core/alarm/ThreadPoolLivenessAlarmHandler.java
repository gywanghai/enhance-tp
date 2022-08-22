package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/19
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ThreadPoolLivenessAlarmHandler implements AlarmEventHandler {

    @Override
    public void handler(AlarmInfo alarmInfo, ThreadPoolExecutorWrapper threadPoolExecutorWrapper) {
        log.info("线程池活跃度告警: {}", alarmInfo);
    }
}
