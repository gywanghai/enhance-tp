package com.ocean.enhancetp.common.event;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 使用 Guava 实现的事件发布器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/9
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class DefaultEventPublisher implements EventPublisher {

    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue<>();

    private Map<String, EventListener<Event>> eventListenerMap = new ConcurrentHashMap<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("event-dispatcher", false));

    private AtomicBoolean running = new AtomicBoolean(true);

    public DefaultEventPublisher(){
        scheduledThreadPoolExecutor.execute(() -> {
            while (running.get()){
                try {
                    Event event = eventLinkedBlockingQueue.take();
                    EventListener eventListener = eventListenerMap.get(event.getSource());
                    if(eventListener != null){
                        eventListener.onMessage(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("中断异常", e.getMessage());
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
            Thread.currentThread().interrupt();
            log.error("中断异常", e);
        }
    }

    @Override
    public void stop() {
        running.set(false);
    }
}
