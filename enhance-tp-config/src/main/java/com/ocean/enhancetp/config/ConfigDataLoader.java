package com.ocean.enhancetp.config;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @description: 远配置数据加载器接口，定义配置数据的加载接口
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/25
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface ConfigDataLoader {

    public void init(ConfigurableApplicationContext context, String application, String env);

    public String getConfigData(String application, String env, String dataId);
}
