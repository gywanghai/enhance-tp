package com.ocean.enhancetp.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @description: 领域事件基类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
@AllArgsConstructor
public class Event<T> {
    /**
     * 事件ID
     */
    private String id;
    /**
     * 事件源
     */
    private String source;
    /**
     * 事件数据
     */
    private T data;
    /**
     * 事件日期
     */
    private Date eventDate;
}
