package com.ocean.enhancetp.datasource;

import com.ocean.enhancetp.config.*;
import com.ocean.enhancetp.config.listener.PropertyListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 抽象数据源
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public abstract class AbstractDataSource<S, T> implements ReadableDataSource<S, T>, PropertyListener<T>, ApplicationContextAware, EnvironmentAware {
    
    protected Converter<S, T> parser;
    
    protected Map<String, CommonProperty> propertyMap;

    protected ApplicationContext applicationContext;

    protected Environment environment;

    protected DataIdGenerator dataIdGenerator = (application, profile, originId) -> {
        if(StringUtils.isEmpty(profile)){
            return StringUtils.join(application, "-", originId);
        }
        return StringUtils.join(application, "-", originId, "-", profile);
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected AbstractDataSource(Converter<S, T> parser) {
        if(parser == null){
            throw new IllegalArgumentException("parser can't be null");
        }
        this.parser = parser;
        this.propertyMap = new ConcurrentHashMap<>();
    }


    @Override
    public T loadConfig(String dataId)  {
        propertyMap.computeIfAbsent(dataId, s -> {
            DynamicProperty dynamicProperty = new DynamicProperty();
            dynamicProperty.setDataId(dataId);
            dynamicProperty.addListener(AbstractDataSource.this);
            return dynamicProperty;
        });
        T value = convert(readSource(dataId));
        if(value != null){
            getProperty(dataId).updateValue(value);
        }
        return value;
    }

    public T convert(S conf) {
        return parser.convert(conf);
    }

    @Override
    public CommonProperty<T> getProperty(String dataId) {
        return propertyMap.get(dataId);
    }

    @Override
    public void configUpdate(String dataId, T oldValue, T value) {
        applicationContext.publishEvent(new ConfigUpdateEvent(new ConfigChangeRecord<T>(dataId, oldValue, value)));
    }

    @Override
    public void configLoad(String dataId, T oldValue, T value) {
        applicationContext.publishEvent(new ConfigUpdateEvent(new ConfigChangeRecord<T>(dataId, oldValue, value)));
    }

    protected String getFormatDataId(String originId){
        String application = environment.getProperty("spring.application.name");
        String profile = environment.getActiveProfiles().length == 0? "default" : environment.getActiveProfiles()[0];
        return dataIdGenerator.generator(application, profile, originId);
    }
}
