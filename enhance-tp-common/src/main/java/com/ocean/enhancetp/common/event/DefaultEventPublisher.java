package com.ocean.enhancetp.common.event;

import cn.hutool.core.thread.NamedThreadFactory;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description: 使用 Guava 实现的事件发布器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class DefaultEventPublisher implements EventPublisher {

    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue<>(65535);

    private Map<String, EventListener> eventListenerMap = new ConcurrentHashMap<>();

    public ExecutorService executorService = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("event-dispatcher", false));

    public DefaultEventPublisher(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Event event = eventLinkedBlockingQueue.take();
                        EventListener eventListener = eventListenerMap.get(event.getSource());
                        if(eventListener != null){
                            eventListener.onMessage(event);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
    @Override
    public void registerEventListener(String source, EventListener eventListener){
        eventListenerMap.put(source, eventListener);
    }

    @Override
    public void unregisterEventListener(String source){
        eventListenerMap.remove(source);
    }

    @Override
    public void publishEvent(Event event) {
        try {
            eventLinkedBlockingQueue.put(event);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
