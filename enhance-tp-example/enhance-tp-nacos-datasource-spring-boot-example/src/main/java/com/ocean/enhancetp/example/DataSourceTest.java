package com.ocean.enhancetp.example;

import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/9/2
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
@Component
public class DataSourceTest implements ApplicationRunner {

    @Autowired
    private AbstractDataSource<String, ThreadPoolExecutorProperty> dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ThreadPoolExecutorProperty threadPoolExecutorProperty = dataSource.loadConfig("test");
        log.info("ThreadPoolExecutorProperty: {}", threadPoolExecutorProperty);
    }
}
