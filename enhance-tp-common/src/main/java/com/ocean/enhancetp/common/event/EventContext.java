package com.ocean.enhancetp.common.event;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/18
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class EventContext {

    private EventContext(){}

    private static EventPublisher eventPublisher;

    public static void init(EventPublisher eventPublisher){
        EventContext.eventPublisher = eventPublisher;
    }

    public static void publishEvent(Event<?> event){
        if(null != eventPublisher){
            eventPublisher.publishEvent(event);
        }
    }
}
