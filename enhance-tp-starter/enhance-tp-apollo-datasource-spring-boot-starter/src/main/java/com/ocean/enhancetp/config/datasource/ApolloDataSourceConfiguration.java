package com.ocean.enhancetp.config.datasource;

import com.ocean.enhancetp.common.property.YamlPropertyParser;
import com.ocean.enhancetp.config.properties.ApolloDataSourceProperties;
import com.ocean.enhancetp.autoconfig.EnhanceTpProperties;
import com.ocean.enhancetp.core.properties.ThreadPoolExecutorProperty;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import com.ocean.enhancetp.datasource.apollo.ApolloDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/9/1
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
@Configuration
public class ApolloDataSourceConfiguration {

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
    public AbstractDataSource apolloDataSource(EnhanceTpProperties enhanceTpProperties, Environment environment){
        ApolloDataSourceProperties dataSourceProperties = enhanceTpProperties.getDatasource().getApollo();
        String env = environment.getActiveProfiles().length == 0? "default" : environment.getActiveProfiles()[0];
        if(dataSourceProperties == null){
            throw new IllegalStateException("please config apollo datasource properties");
        }
        return new ApolloDataSource(dataSourceProperties.getAppId(),dataSourceProperties.getNamespace(), env, dataSourceProperties.getMeta(), parser());
    }
}
