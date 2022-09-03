package com.ocean.enhancetp.core;

import com.ocean.enhancetp.config.properties.*;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/30
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
@Slf4j
@Component
@ConfigurationProperties(prefix = "spring.enhancetp")
public class EnhanceTpProperties {

    private DataSourcePropertiesConfig datasource;

    @Data
    @ToString
    public static class DataSourcePropertiesConfig {

        private String threadPoolId;

        private FileDataSourceProperties file;

        private NacosDataSourceProperties nacos;

        private ZookeeperDatasourceProperties zookeeper;

        private ApolloDataSourceProperties apoll;

        private RedisDataSourceProperties redis;

        private ConsulDataSourceProperties consul;

        public List<String> validFields() {
            return Arrays.stream(this.getClass().getDeclaredFields()).map(field -> {
                try {
                    if (!ObjectUtils.isEmpty(field.get(this))) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    // won't happen
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }

        public AbstractDataSourceProperties validDatasourceProperties() {
            List<String> validFields = validFields();
            AbstractDataSourceProperties dataSourceProperties = null;
            if (validFields.size() == 1) {
                String validFieldName = validFields.get(0);
                Field validField = null;
                try {
                    validField = this.getClass().getDeclaredField(validFieldName);
                    validField.setAccessible(true);
                    dataSourceProperties = (AbstractDataSourceProperties) validField.get(this);
                    validField.setAccessible(false);
                } catch (NoSuchFieldException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
                return dataSourceProperties;
            }
            return null;
        }
    }
}
