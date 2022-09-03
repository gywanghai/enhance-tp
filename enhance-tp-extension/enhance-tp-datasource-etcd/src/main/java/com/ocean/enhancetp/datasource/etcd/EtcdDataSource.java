package com.ocean.enhancetp.datasource.etcd;

import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class EtcdDataSource<T> extends AbstractDataSource<String, T> {

    private Client client;

    private Watch.Watcher watcher;

    private String dataId;

    private Charset charset = StandardCharsets.UTF_8;

    private String endpoints;

    private boolean authEnable;

    private String user;

    private String password;
    private String authority;

    public EtcdDataSource(String dataId, Converter<String, T> parser) {
        super(parser);
        if(authEnable){
            this.client = Client.builder().endpoints(endpoints.split(","))
                    .authority(authority)
                    .user(ByteSequence.from(user, charset))
                    .password(ByteSequence.from(password, charset))
                    .build();
        }
        else {
            this.client = Client.builder()
                    .endpoints(endpoints.split(","))
                    .build();
        }
        this.dataId = dataId;
        loadInitialConfig();
        initWatcher();
    }

    private void loadInitialConfig() {
//        try{
//            T newValue = loadConfig();
//            if(newValue == null){
//                log.error("[EtcdDataSource] Initial configuration is null, you may have to check your datasource");
//                return;
//            }
//            getProperty().updateValue(newValue);
//        }catch (Exception e){
//            log.error("[EtcdDatasource] Error when loading initial configuration", e);
//        }
    }

    private void initWatcher() {
        watcher = client.getWatchClient().watch(ByteSequence.from(dataId, charset), (watchResponse -> {
            for(WatchEvent watchEvent : watchResponse.getEvents()){
                WatchEvent.EventType eventType = watchEvent.getEventType();
                if(eventType == WatchEvent.EventType.PUT){
//                    try{
//                        T newValue = loadConfig();
//                        if(newValue == null){
//                            return;
//                        }
//                        getProperty().updateValue(newValue);
//                    }catch (Exception e){
//                        log.error("[EtcdDataSource] Failed to update config", e);
//                    }
                }
            }
        }));
    }

    @Override
    public String readSource(String dataId) {
        String content = null;
        List<KeyValue> kvs = null;
        try {
            CompletableFuture<GetResponse> responseCompleteFuture = client.getKVClient().get(ByteSequence.from(dataId, charset));
            kvs = responseCompleteFuture.get().getKvs();
            content = kvs.isEmpty() ? null : kvs.get(0).getValue().toString(charset);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[EtcdDataSource] load config from datasource Fail ", e);
        } catch (ExecutionException e) {
            log.error("[EtcdDataSource] load config from datasource Fail ", e);
        }
        return content;
    }

    @Override
    public void close() {
        if(watcher != null){
            watcher.close();
        }
        if(client != null){
            client.close();
        }
    }
}
