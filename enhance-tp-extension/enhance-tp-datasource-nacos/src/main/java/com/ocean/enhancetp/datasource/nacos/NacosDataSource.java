package com.ocean.enhancetp.datasource.nacos;

import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Preconditions;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @description: Nacos 数据源
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class NacosDataSource<T> extends AbstractDataSource<String, T> {

    private static final int DEFAULT_TIMEOUT = 3000;

    private ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(65536),
            new NamedThreadFactory("enhancetp-nacos-ds-update-", true), new ThreadPoolExecutor.DiscardOldestPolicy());

    private Map<String, Listener> configListenerMap = new ConcurrentHashMap<>();

    private String groupId;

    private Properties properties;

    private ConfigService configService;

    public NacosDataSource(String serverAddr, String groupId, Converter<String, T> parser) {
        this(NacosDataSource.buildProperties(serverAddr),groupId, parser);
    }

    public NacosDataSource(Properties properties, String groupId, Converter<String, T> parser){
        super(parser);
        Preconditions.checkArgument(StringUtils.isNotBlank(groupId), String.format("Bad argument: groupId=[%s]", groupId));
        Preconditions.checkArgument(properties != null, "Nacos properties must not be null");
        this.groupId = groupId;
        this.properties = properties;
        initNacosConfigService();
    }

    public void initNacosConfigService(){
        try{
            this.configService = NacosFactory.createConfigService(this.properties);
        }catch (Exception e){
            log.error("[NacosDataSource] Error occurred when initializing Nacos data source", e);
        }
    }

    private static Properties buildProperties(String serverAddr) {
        Properties prop = new Properties();
        if(StringUtils.isNotBlank(serverAddr)){
            prop.put("serverAddr", serverAddr);
            return prop;
        }
        return prop;
    }

    @Override
    public String readSource(String dataId) {
        String content = null;
        if(configService == null){
            throw new IllegalStateException("Nacos config service has not been initialized or error occurred");
        }
        try {
            configListenerMap.computeIfAbsent(dataId, s -> new ConfigChangeListener(dataId, groupId));
            content = configService.getConfigAndSignListener(getFormatDataId(dataId), groupId, DEFAULT_TIMEOUT, configListenerMap.get(dataId));
        } catch (NacosException e) {
            log.error("[Nacos] Error when load config[dataId: {}] config data", dataId);
        }
        return content;
    }

    @Override
    public void close() {
        if(configService != null){
            configListenerMap.values().stream().forEach(listener -> {
                ConfigChangeListener configListener = (ConfigChangeListener)listener;
                configService.removeListener(configListener.getDataId(), configListener.getGroupId(), listener);
            });
        }
        pool.shutdown();
        configListenerMap.clear();
        if(configService != null){
            try {
                configService.shutDown();
            } catch (NacosException e) {
                log.error("[NacosDataSource] Error when close datasource", e);
            }
        }
    }

    @Data
    public class ConfigChangeListener implements Listener {

        private String dataId;

        private String groupId;

        private ConfigChangeListener(String dataId, String groupId){
            this.dataId = dataId;
            this.groupId = groupId;
        }

        @Override
        public Executor getExecutor() {
            return pool;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            loadConfig(dataId);
        }
    }
}
