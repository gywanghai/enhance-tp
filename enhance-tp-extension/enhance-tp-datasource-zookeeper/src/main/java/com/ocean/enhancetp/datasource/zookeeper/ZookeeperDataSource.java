package com.ocean.enhancetp.datasource.zookeeper;

import cn.hutool.core.thread.NamedThreadFactory;
import com.google.common.base.Preconditions;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ZookeeperDataSource<T> extends AbstractDataSource<String, T> {

    public static final String SERVER_ADDR_MUST_NOT_BE_NULL = "serverAddr must not be null";
    private static final int RETRY_TIMES = 3;

    private static final int SLEEP_TIME = 1000;

    private static volatile Map<String, CuratorFramework> zkClientMap = new HashMap<>();

    private static final Object lock = new Object();

    private ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1), new NamedThreadFactory("enhancetp-zookeeper=ds-update-", true),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private String path;

    private NodeCacheListener listener;

    private CuratorFramework zkClient = null;

    private NodeCache nodeCache = null;

    public ZookeeperDataSource(String serverAddr, String path, Converter<String, T> parser) {
        super(parser);
        Preconditions.checkArgument(StringUtils.isNotBlank(serverAddr), SERVER_ADDR_MUST_NOT_BE_NULL);
        Preconditions.checkArgument(StringUtils.isNotBlank(path), "path must not be null");
        this.path = path;

        init(serverAddr, null);
    }

    public ZookeeperDataSource(String serverAddr, String groupId, String dataId, Converter<String, T> parser){
        super(parser);
        Preconditions.checkArgument(StringUtils.isNotBlank(serverAddr), SERVER_ADDR_MUST_NOT_BE_NULL);
        Preconditions.checkArgument(StringUtils.isNotBlank(groupId), "groupId must not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(dataId), "dataId must not be null");

        this.path = getPath(groupId, dataId);
        init(serverAddr, null);
    }

    public ZookeeperDataSource(String serverAddr, List<AuthInfo> authInfos, String groupId, String dataId, Converter<String, T> parser){
        super(parser);
        Preconditions.checkArgument(StringUtils.isNotBlank(serverAddr), SERVER_ADDR_MUST_NOT_BE_NULL);
        Preconditions.checkArgument(StringUtils.isNotBlank(groupId), "groupId must not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(dataId), "dataId must not be null");

        this.path = getPath(groupId, dataId);
        init(serverAddr, authInfos);
    }

    private String getPath(String groupId, String dataId) {
        return String.format("/%s/%s", groupId, dataId);
    }

    private void init(String serverAddr, List<AuthInfo> authInfos) {
        initZookeeperListener(serverAddr, authInfos);
        loadInitialConfig();
    }

    private void initZookeeperListener(String serverAddr, List<AuthInfo> authInfos) {
        this.listener = new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                try
                {
//                    T newValue = loadConfig();
//                    if(newValue != null){
//                        log.info("[ZookeeperDatasource] New property value received for({},{}):{}", serverAddr, path, newValue);
//                        getProperty().updateValue(newValue);
//                    }
                }catch (Exception e){
                    log.error("[ZookeeperDatasource] loadConfig exception", e);
                }
            }
        };

        String zkKey = getZkKey(serverAddr, authInfos);
        if(zkClientMap.containsKey(zkKey)){
            this.zkClient = zkClientMap.get(zkKey);
        }
        else {
            synchronized (lock){
                if(!zkClientMap.containsKey(zkKey)){
                    CuratorFramework zc = null;
                    if(authInfos == null || authInfos.isEmpty()){
                        zc = CuratorFrameworkFactory.newClient(serverAddr, new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
                    }
                    else {
                        zc = CuratorFrameworkFactory.builder()
                                .connectString(serverAddr)
                                .retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES))
                                .authorization(authInfos)
                                .build();
                    }
                    this.zkClient = zc;
                    this.zkClient.start();
                    Map<String, CuratorFramework> newZkClientMap = new HashMap<>(zkClientMap.size());
                    newZkClientMap.putAll(zkClientMap);
                    newZkClientMap.put(zkKey, zc);
                    zkClientMap = newZkClientMap;
                }
                else {
                    this.zkClient = zkClientMap.get(zkKey);
                }
            }
        }
        this.nodeCache = new NodeCache(this.zkClient, this.path);
        this.nodeCache.getListenable().addListener(this.listener, this.pool);
        try {
            this.nodeCache.start();
        } catch (Exception e) {
            log.error("[ZookeeperDataSource] Error occurred when initialzing Zookeeper data soruce", e);
        }
    }

    private String getZkKey(String serverAddr, List<AuthInfo> authInfos) {
        if(authInfos == null || authInfos.isEmpty()){
            return serverAddr;
        }
        StringBuilder builder = new StringBuilder(64);
        builder.append(serverAddr);
        builder.append(getAuthInfosKey(authInfos));
        return builder.toString();
    }

    private String getAuthInfosKey(List<AuthInfo> authInfos) {
        StringBuilder builder = new StringBuilder(32);
        for(AuthInfo authInfo : authInfos){
            if(authInfo == null){
                builder.append("{}");
            }
            else {
                builder.append("{" + "sc=" + authInfo.getScheme() + ",au=" + Arrays.toString(authInfo.getAuth()) + "}");
            }
        }
        return builder.toString();
    }

    private void loadInitialConfig() {
//        try{
//            T newValue = loadConfig();
//            if(newValue == null){
//                log.error("[ZookeeperDataSource] WARN: initial config is null, you may have to check you data source");
//                return;
//            }
//            getProperty().updateValue(newValue);
//        }catch (Exception e){
//            log.error("[ZookeeperDatasource] Error when loding initial config", e);
//        }
    }

    @Override
    public String readSource(String dataId)  {
        if(this.zkClient == null){
            throw new IllegalStateException("Zookeeper has not be initialized");
        }
        String configInfo = null;
        ChildData childData = nodeCache.getCurrentData();
        if(null != childData && childData.getData() != null){
            configInfo = new String(childData.getData());
        }
        return configInfo;
    }

    @Override
    public void close() {
        try {
            if(this.nodeCache != null){
                this.nodeCache.getListenable().removeListener(listener);
                this.nodeCache.close();
            }
            if(this.zkClient != null){
                this.zkClient.close();
            }
        } catch (IOException e) {
            log.error("[ZookeeperDatasource] Error when close datasource", e);
        }
        pool.shutdown();
    }
}
