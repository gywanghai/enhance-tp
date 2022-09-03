package com.ocean.enhancetp.datasource.redis;

import com.google.common.base.Preconditions;
import com.ocean.enhancetp.datasource.AbstractDataSource;
import com.ocean.enhancetp.datasource.Converter;
import com.ocean.enhancetp.datasource.redis.config.RedisConnectionConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class RedisDataSource<T> extends AbstractDataSource<String, T> {

    private RedisClient redisClient;

    private RedisClusterClient redisClusterClient;

    public RedisDataSource(RedisConnectionConfig connectionConfig, String dataId, String channel, Converter<String, T> parser) {
        super(parser);
        Preconditions.checkArgument(connectionConfig != null, "Redis connection config can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(dataId), "Redis dataId can not be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(channel), "Redis subscribe channel can not be empty");
        if(connectionConfig.getRedisClusters().size() == 0){
            this.redisClient = getRedisClient(connectionConfig);
            this.redisClusterClient = null;
        }
        else {
            this.redisClusterClient = getRedisClusterClient(connectionConfig);
            this.redisClient = null;
        }
        loadInitialConfig();
        subscribeFromChannel(channel);
    }

    private RedisClusterClient getRedisClusterClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        List<RedisURI> redisURIS = new ArrayList<>();
        for(RedisConnectionConfig config: connectionConfig.getRedisClusters()){
            RedisURI.Builder clusterRedisUriBuilder = RedisURI.builder();
            clusterRedisUriBuilder.withHost(config.getHost()).withPort(config.getPort())
                    .withTimeout(Duration.ofMillis(config.getTimeout()));
            if(password != null){
                clusterRedisUriBuilder.withPassword(config.getPassword());
            }
            redisURIS.add(clusterRedisUriBuilder.build());
        }
        return RedisClusterClient.create(redisURIS);
    }

    private RedisClient getRedisClient(RedisConnectionConfig connectionConfig) {
        if(connectionConfig.getRedisSentinels().size() == 0){
            log.error("[RedisDataSource] Creating stand-alone mode Redis Client");
            return getRedisStandaloneClient(connectionConfig);
        }
        else {
            log.error("[RedisDataSource] Creating Redis Sentinel mode Redis Client");
            return getRedisSentinelClient(connectionConfig);
        }
    }

    private RedisClient getRedisStandaloneClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();
        RedisURI.Builder redisUriBuilder = RedisURI.builder();
        redisUriBuilder.withHost(connectionConfig.getHost())
                .withPort(connectionConfig.getPort())
                .withDatabase(connectionConfig.getDatabase())
                .withTimeout(Duration.ofMillis(connectionConfig.getTimeout()));
        if(password != null){
            redisUriBuilder.withPassword(password);
        }
        if(StringUtils.isNotBlank(connectionConfig.getClientName())){
            redisUriBuilder.withClientName(clientName);
        }
        return RedisClient.create(redisUriBuilder.build());
    }

    private RedisClient getRedisSentinelClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();
        RedisURI.Builder sentinelRedisUriBuilder = RedisURI.builder();
        for(RedisConnectionConfig config: connectionConfig.getRedisSentinels()){
            sentinelRedisUriBuilder.withSentinel(config.getHost(), config.getPort());
        }
        if(password != null){
            sentinelRedisUriBuilder.withPassword(password);
        }
        if(StringUtils.isNotBlank(connectionConfig.getClientName())){
            sentinelRedisUriBuilder.withClientName(clientName);
        }
        sentinelRedisUriBuilder.withSentinelMasterId(connectionConfig.getRedisSentinelMasterId());
        sentinelRedisUriBuilder.withTimeout(Duration.ofMillis(connectionConfig.getTimeout()));
        return RedisClient.create(sentinelRedisUriBuilder.build());
    }

    private void loadInitialConfig() {
//        try{
//            T newValue = loadConfig();
//            if(newValue == null){
//                log.error("[RedisDataSource] WARN: initial config is null, you may have to check your datasource");
//                return;
//            }
//            getProperty().updateValue(newValue);
//        }catch (Exception e){
//            log.error("[RedisDataSource] Error when loading initial config", e);
//        }
    }

    private void subscribeFromChannel(String channel) {
//        RedisPubSubAdapter<String,String > adapterListener = new DelegatingRedisPubSubListener();
//        if(redisClient != null){
//            StatefulRedisPubSubConnection<String,String> pubSubConnection = redisClient.connectPubSub();
//            pubSubConnection.addListener(adapterListener);
//            RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
//            sync.subscribe(channel);
//         }
//        else {
//            StatefulRedisClusterPubSubConnection<String, String> pubSubConnection = redisClusterClient.connectPubSub();
//            pubSubConnection.addListener(adapterListener);
//            RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
//            sync.subscribe(channel);
//        }
    }

    @Override
    public String readSource(String dataId) {
        if(this.redisClient == null && this.redisClusterClient == null){
            log.error("Redis client or Redis Cluster client has not been initialized or error occurred");
            return null;
        }
        if(redisClient != null){
            RedisCommands<String, String> stringRedisCommands = redisClient.connect().sync();
            return stringRedisCommands.get(dataId);
        }
        else {
            RedisAdvancedClusterCommands<String, String> stringRedisCommands = redisClusterClient.connect().sync();
            return stringRedisCommands.get(dataId);
        }
    }

    @Override
    public void close()  {
        if(redisClient != null){
            redisClient.shutdown();
        }
        else {
            redisClusterClient.shutdown();
        }
    }

    private class DelegatingRedisPubSubListener extends RedisPubSubAdapter<String, String> {

        private String dataId;

        DelegatingRedisPubSubListener(String dataId){
            this.dataId = dataId;
        }

        @Override
        public void message(String channel, String message) {
            log.error("[RedisDataSource] New property value received for channel {}: {}", channel, message);
            getProperty(dataId).updateValue(parser.convert(message));
        }
    }
}
