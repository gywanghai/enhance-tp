package com.ocean.enhancetp.core.alarm;

import com.ocean.enhancetp.common.spi.SpiOrder;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/22
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@SpiOrder
public class TaskWaitTimeoutAlarmHandlerFactory implements AlarmEventHandlerFactory{

    @Override
    public AlarmType alarmType() {
        return AlarmType.WAIT_TIMEOUT;
    }

    @Override
    public AlarmEventHandler getAlarmEventHandler() {
        return new TaskWaitTimeoutAlarmHandler();
    }
}
