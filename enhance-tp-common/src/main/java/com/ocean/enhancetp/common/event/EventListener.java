package com.ocean.enhancetp.common.event;

/**
 * @description: 事件监听器接口
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface EventListener<T> {

    /**
     * 接收到事件消息的回调方法
     * @param event
     */
    public void onMessage(Event<T> event);

}
