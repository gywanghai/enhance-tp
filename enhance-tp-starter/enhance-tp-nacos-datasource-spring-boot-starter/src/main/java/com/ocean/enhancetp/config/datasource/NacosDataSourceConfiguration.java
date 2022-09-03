package com.ocean.enhancetp.config.datasource;

import com.alibaba.nacos.client.config.impl.YmlChangeParser;
import com.ocean.enhancetp.common.property.YamlPropertyParser;
import com.ocean.enhancetp.config.DynamicProperty;
import com.ocean.enhancetp.config.properties.NacosDataSourceProperties;
import com.ocean.enhancetp.core.EnhanceTpProperties;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import com.ocean.enhancetp.datasource.nacos.NacosDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/9/1
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
@Configuration
public class NacosDataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean(Converter.class)
    public Converter parser(){
        return (Converter<String, ThreadPoolExecutorProperty>) source -> {
            log.info("source:{}", source);
            return new YamlPropertyParser().parseObject(source, ThreadPoolExecutorProperty.class);
        };
    }


    @Bean
    @ConditionalOnMissingBean(AbstractDataSource.class)
    public AbstractDataSource nacosDataSource(EnhanceTpProperties enhanceTpProperties){
        NacosDataSourceProperties nacosDataSourceProperties = enhanceTpProperties.getDatasource().getNacos();
        String serverAddr = nacosDataSourceProperties.getServerAddr();
        String groupId = nacosDataSourceProperties.getGroupId();
        return new NacosDataSource(serverAddr, groupId, parser());
    }

}
