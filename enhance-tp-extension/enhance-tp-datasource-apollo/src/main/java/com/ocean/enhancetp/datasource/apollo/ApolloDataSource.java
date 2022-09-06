package com.ocean.enhancetp.datasource.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.ocean.enhancetp.config.CommonProperty;
import com.ocean.enhancetp.config.ConfigUpdateEvent;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @description: Apollo 数据源
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ApolloDataSource<T> extends AbstractDataSource<String, T> {

    private Config config;

    private final Map<String, ConfigChangeListener> configChangeListenerMap = new ConcurrentHashMap<>();

    public ApolloDataSource(String appId, String namespace, String env, String meta,Converter<String, T> parser) {
        super(parser);
        System.setProperty("apollo.meta", meta);
        System.setProperty("app.id", appId);
        System.setProperty("env", env);
        Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "namespace could not be null or empty");

        this.config = ConfigService.getConfig(namespace);

        log.info("Initialize config for namespace : {}", namespace);
    }


    private void loadAndUpdateRules(String dataId) {
        try{
            T newValue = loadConfig(dataId);
            if(newValue == null){
                log.error("[ApolloDataSource] WARN: config is null, you may have to check your data source");
                return;
            }
            getProperty(dataId).updateValue(newValue);
        }
        catch (Exception e){
            log.error("[ApolloDataSource] Error when loading config", e);
        }
    }


    @Override
    public String readSource(String dataId) {
        configChangeListenerMap.computeIfAbsent(dataId, s -> {
            ConfigChangeListener changeListener = changeEvent -> {
                ConfigChange change = changeEvent.getChange(getFormatDataId(dataId));
                if(change != null){
                    loadAndUpdateRules(dataId);
                }
            };
            config.addChangeListener(changeListener, Sets.newHashSet(getFormatDataId(dataId)));
            return changeListener;
        });
        return config.getProperty(getFormatDataId(dataId), "");
    }

    @Override
    public void close()  {
        configChangeListenerMap.values().stream().forEach(configChangeListener -> config.removeChangeListener(configChangeListener));
    }
}
