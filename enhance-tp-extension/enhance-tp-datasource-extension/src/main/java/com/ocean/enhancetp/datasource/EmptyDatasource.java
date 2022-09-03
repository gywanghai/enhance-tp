package com.ocean.enhancetp.datasource;

import com.ocean.enhancetp.config.CommonProperty;
import com.ocean.enhancetp.config.NoOpProperty;

/**
 * @description: 当我们想用默认配置替换来自可读数据源的设置时使用这个类
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class EmptyDatasource implements ReadableDataSource<Object, Object>{

    public static final ReadableDataSource<Object, Object> EMPTY_DATASOURCE = new EmptyDatasource();

    private static final CommonProperty<Object> PROPERTY = new NoOpProperty();


    @Override
    public Object loadConfig(String dataId) {
        return null;
    }

    @Override
    public Object readSource(String id) {
        return null;
    }

    @Override
    public CommonProperty<Object> getProperty(String dataId) {
        return PROPERTY;
    }

    @Override
    public void close() {
        // ignore
    }
}
