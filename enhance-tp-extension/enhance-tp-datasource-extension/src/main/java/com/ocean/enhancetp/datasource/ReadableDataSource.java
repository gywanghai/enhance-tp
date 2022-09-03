package com.ocean.enhancetp.datasource;

import com.ocean.enhancetp.config.CommonProperty;

/**
 * 可读数据源
 * @param <S>
 * @param <T> 配置信息
 */
public interface ReadableDataSource<S, T> {
    /**
     * 加载配置
     */
    T loadConfig(String dataId);

    /**
     * 数据源读取的原始数据
     */
    S readSource(String dataId);

    /**
     * 获取线程池属性
     * @return
     */
    CommonProperty<T> getProperty(String dataId);

    /**
     * 关闭数据源
     * @throws Exception
     */
    void close();
}
