package com.ocean.enhancetp.core.blockingqueue;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 可修改容量的 LinkedBlockingQueue
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/19
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ResizableLinkedBlockingQueue<T> extends LinkedBlockingQueue<T> {

    public ResizableLinkedBlockingQueue(int capacity){
        super(capacity);
    }

    /**
     * 通过反射修改队列容量
     * @param capacity
     */
    public void setCapacity(int capacity){
        Field field = ReflectUtil.getField(this.getClass(), "capacity");
        field.setAccessible(true);
        try {
            int oldCapacity = field.getInt(this);
            field.setInt(this, capacity);
            AtomicInteger count = (AtomicInteger) ReflectUtil.getFieldValue(this, "count");
            int size = count.get();

            ReflectUtil.setFieldValue(this, "capacity", capacity);
            if (capacity > size && size >= oldCapacity) {
                ReflectUtil.invoke(this, "signalNotFull");
            }
        } catch (IllegalAccessException e) {
            log.error("修改队列容量失败", e);
        }finally {
            field.setAccessible(false);
        }

    }
}
