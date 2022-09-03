package com.ocean.enhancetp.example;

import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.nacos.NacosDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/9/2
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
@Component
public class NacosDataSourceTest implements SmartInitializingSingleton {

    @Autowired
    private AbstractDataSource<String, ThreadPoolExecutorProperty> dataSource;

    @Override
    public void afterSingletonsInstantiated() {
        ThreadPoolExecutorProperty threadPoolExecutorProperty = dataSource.loadConfig("test");
        log.info("ThreadPoolExecutorProperty: {}", threadPoolExecutorProperty);
    }
}
