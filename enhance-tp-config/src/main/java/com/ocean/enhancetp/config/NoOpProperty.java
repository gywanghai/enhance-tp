package com.ocean.enhancetp.config;

import com.ocean.enhancetp.config.listener.PropertyListener;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class NoOpProperty implements CommonProperty<Object> {

    @Override
    public void addListener(PropertyListener<Object> listener) {
        // ignore
    }

    @Override
    public void removeListener(PropertyListener<Object> listener) {
        // ignore
    }

    @Override
    public boolean updateValue(Object value) {
        return true;
    }
}
