package com.ocean.enhancetp.datasource;

/**
 * @description: 写数据源接口
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface WritableDataSource<T> {

    void write(T value);

    void close();
}
