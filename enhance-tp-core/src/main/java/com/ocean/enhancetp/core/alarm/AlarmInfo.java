package com.ocean.enhancetp.core.alarm;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @description: 告警信息
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
public class AlarmInfo {
    /**
     * 应用名
     */
    private String application;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 线程池ID
     */
    private String threadPoolId;
    /**
     * 报警类型
     */
    private AlarmType alarmType;
    /**
     * 报警时间
     */
    private Date date;
    /**
     * 附加信息
     */
    private Object data;
}
