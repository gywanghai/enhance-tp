package com.ocean.enhancetp.config.listener;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface PropertyListener<T> {

    void configUpdate(String dataId, T oldValue, T value);

    void configLoad(String dataId,T oldValue, T value);
}
