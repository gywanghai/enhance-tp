package com.ocean.enhancetp.common.event;

/**
 * @description: 事件发布器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface EventPublisher {
    /**
     * 发布事件
     * @param event 事件
     */
    void publishEvent(Event<? extends Object> event);

    /**
     * 注册事件监听器
     * @param source
     * @param eventListener
     */
    default void registerEventListener(String source, EventListener eventListener){}

    /**
     * 注销事件监听器
     */
    default  void unregisterEventListener(String source){}

    /**
     * 停止监听
     */
    void stop();
}
