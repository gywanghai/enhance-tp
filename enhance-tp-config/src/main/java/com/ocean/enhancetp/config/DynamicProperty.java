package com.ocean.enhancetp.config;

import com.ocean.enhancetp.config.listener.PropertyListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@Slf4j
public class DynamicProperty<T> implements CommonProperty<T> {

    protected Set<PropertyListener<T>> listeners = new CopyOnWriteArraySet<>();

    private String dataId;

    private T value = null;

    public DynamicProperty(){}

    public DynamicProperty(String dataId){
        this.dataId = dataId;
    }

    public DynamicProperty(String dataId, T value){
        this.dataId = dataId;
        this.value = value;
    }

    @Override
    public void addListener(PropertyListener<T> listener) {
        listeners.add(listener);
        listener.configLoad(dataId, null, value);
    }

    @Override
    public void removeListener(PropertyListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean updateValue(T newValue) {
        if(isEqual(value, newValue)){
            return false;
        }
        log.info("[CommonProperty] Config will be updated to : {}", newValue);
        for(PropertyListener<T> listener : listeners){
            if(value != null){
                listener.configUpdate(dataId, value, newValue);
            }
        }
        value = newValue;
        return true;
    }

    private boolean isEqual(T oldValue, T newValue){
        if(oldValue == null && newValue == null){
            return true;
        }
        if(oldValue == null){
            return false;
        }
        return oldValue.equals(newValue);
    }

    public void close() {
        listeners.clear();
    }
}
