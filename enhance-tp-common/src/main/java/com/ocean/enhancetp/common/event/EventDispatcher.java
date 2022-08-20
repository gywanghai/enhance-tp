package com.ocean.enhancetp.common.event;

import com.google.common.eventbus.Subscribe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 事件分派器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/10
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class EventDispatcher implements EventListener {

    private Map<String, EventListener> eventListenerMap = new ConcurrentHashMap<>();

    public void registerEventListener(String source, EventListener eventListener){
        eventListenerMap.put(source, eventListener);
    }

    @Subscribe
    @Override
    public void onMessage(Event event) {
        dispatch(event);
    }

    public void dispatch(Event<Object> event){
        String source = event.getSource();
        // 根据不同的时间源，调用不同的监听器
        EventListener eventListener = eventListenerMap.get(source);
        if(null != eventListener){
            eventListener.onMessage(event);
        }
    }

    public void unregisterEventListener(String source) {
        eventListenerMap.remove(source);
    }
}
