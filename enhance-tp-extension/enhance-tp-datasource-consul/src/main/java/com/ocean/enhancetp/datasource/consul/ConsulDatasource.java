package com.ocean.enhancetp.datasource.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.common.base.Preconditions;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ConsulDatasource<T> extends AbstractDataSource<String, T> {

    private static final int DEFAULT_PROT = 8500;

    private String address;

    private String token;

    private int watchTimeout;

    private volatile long lastIndex;

    private ConsulClient client;

    private Map<String, ConsulKVWatcher> watcherMap = new ConcurrentHashMap<>();

    private ExecutorService watcherService = Executors.newSingleThreadExecutor();


    public ConsulDatasource(String host, int watchTimeoutInSecond,Converter<String, T> parser) {
        this(host, DEFAULT_PROT, watchTimeoutInSecond, parser);
    }

    public ConsulDatasource(String host, int port, int watchTimeoutInSecond, Converter<String, T> parser){
        this(host, port, null, watchTimeoutInSecond, parser);
    }

    public ConsulDatasource(String host, int port, String token, int watchTimeoutInSecond, Converter<String, T> parser){
        super(parser);
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "Consul host can not be null");
        Preconditions.checkArgument(watchTimeoutInSecond >= 0, "watchTimeout should not be negative");
        this.client = new ConsulClient(host, port);
        this.address = host + ":" + port;
        this.watchTimeout = watchTimeoutInSecond;
        this.token = token;
    }

//    private void loadInitialConfig() {
//        try {
//            T newValue = loadConfig();
//            if(newValue == null){
//                log.error("[ConsulDataSource] WARN: inital config is null, you may have to check your data source");
//            }
//            else {
//                getProperty().updateValue(newValue);
//            }
//        }catch (Exception e){
//            log.error("[ConsulDataSource] Error when loading initial config", e);
//        }
//    }

    private void startKVWatcher(ConsulKVWatcher watcher) {
        watcherService.submit(watcher);
    }

    @Override
    public String readSource(String dataId) {
        if(this.client == null){
            throw new IllegalStateException("Consul has not been initialized or error occurred");
        }
        Response<GetValue> response = getValueImmediately(dataId);
        if(response != null){
            GetValue value = response.getValue();
            lastIndex = response.getConsulIndex();
            return value != null ? value.getDecodedValue() : null;
        }
        return null;
    }

    @Override
    public void close() {
        watcherMap.values().stream().forEach(ConsulKVWatcher::stop);
        watcherService.shutdown();
    }

    private class ConsulKVWatcher implements Runnable {

        private String dataId;
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running){
                Response<GetValue> response = getValue(dataId, lastIndex, watchTimeout);
                if(response == null){
                    try {
                        TimeUnit.MILLISECONDS.sleep(watchTimeout * 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                else {
                    GetValue getValue = response.getValue();
                    Long currentIndex = response.getConsulIndex();
                    if(currentIndex == null || currentIndex <= lastIndex){
                        continue;
                    }
                    lastIndex = currentIndex;
                    if(getValue != null){
                        String newValue = getValue.getDecodedValue();
                        getProperty(dataId).updateValue(parser.convert(newValue));
                        log.info("[ConsulDataSource] New property value received for ({}, {}), raw value: {}",
                                address, dataId, newValue);
                    }
                }
            }
        }

        private void stop(){
            running = false;
        }
    }

    private Response<GetValue> getValueImmediately(String dataId){
        return getValue(dataId, -1, -1);
    }

    private Response<GetValue> getValue(String dataId, long lastIndex, int watchTimeout) {
        try{
            if(StringUtils.isNotBlank(token)){
                return client.getKVValue(dataId, token, new QueryParams(watchTimeout, lastIndex));
            }
            else {
                return client.getKVValue(dataId, new QueryParams(watchTimeout, lastIndex));
            }
        }
        catch (Exception e){
            log.error("[ConsulDataSource] Failed to get value for key: " +  dataId, e);
        }
        return null;
    }
}
