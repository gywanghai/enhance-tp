package com.ocean.enhancetp.config;

import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @description: 线程池更新事件
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/26
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@ToString
public class ConfigUpdateEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ConfigUpdateEvent(Object source) {
        super(source);
    }
}
