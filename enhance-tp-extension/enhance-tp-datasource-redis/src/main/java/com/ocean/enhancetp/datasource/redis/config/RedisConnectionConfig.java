package com.ocean.enhancetp.datasource.redis.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.hutool.core.net.NetUtil.isValidPort;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
public class RedisConnectionConfig {

    public static final int DEFAULT_SENTINEL_PORT = 26379;

    public static final int DEFAULT_CLUSTER_PORT = 6379;

    public static final int DEFAULT_REDIS_PORT = 6379;

    public static final long DEFAULT_TIMEOUT_MILLISECONDS = 60 * 1000L;
    public static final String HOST_MUST_NOT_BE_EMPTY = "Host must not be empty";
    public static final String PORT_OUT_OF_RANGE = "Port out of range : %s";

    private String host;

    private String redisSentinelMasterId;

    private int port;

    private int database;

    private String clientName;

    private char[] password;

    private long timeout = DEFAULT_TIMEOUT_MILLISECONDS;

    private List<RedisConnectionConfig> redisSentinels = new ArrayList<>();

    private List<RedisConnectionConfig> redisClusters = new ArrayList<>();

    public RedisConnectionConfig(){}

    public RedisConnectionConfig(String host, int port, long timeout){
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "host must not be null");
        Preconditions.checkArgument(timeout >= 0, "timeout duration must be greater or equal to zero");
        setHost(host);
        setPort(port);
        setTimeout(timeout);
    }

    public static RedisConnectionConfig.Builder builder() {
        return new RedisConnectionConfig.Builder();
    }

    public static class Builder {

        private String host;

        private String redisSentinelMasterId;

        private int port;

        private int database;

        private String clientName;

        private char[] password;

        private long timeout = DEFAULT_TIMEOUT_MILLISECONDS;

        private List<RedisHostAndPort> redisSentinels = new ArrayList<>();

        private List<RedisHostAndPort> redisClusters = new ArrayList<>();

        private Builder(){}

        public static RedisConnectionConfig.Builder redis(String host){
            return redis(host, DEFAULT_REDIS_PORT);
        }

        public static RedisConnectionConfig.Builder redis(String host, int port){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));
            Builder builder = RedisConnectionConfig.builder();
            return builder.withHost(host).withPort(port);
        }

        public static RedisConnectionConfig.Builder redisSentinel(String host, int port){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));
            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisSentinel(host, port);
        }

        public static RedisConnectionConfig.Builder redisSentinel(String host, String masterId){
            return redisSentinel(host, DEFAULT_SENTINEL_PORT, masterId);
        }

        public static RedisConnectionConfig.Builder redisSentinel(String host, int port, String masterId){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withSentinelMasterId(masterId).withRedisSentinel(host, port);
        }

        public RedisConnectionConfig.Builder withRedisSentinel(String host){
            return withRedisSentinel(host, DEFAULT_SENTINEL_PORT);
        }

        public RedisConnectionConfig.Builder withRedisSentinel(String host, int port){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));
            redisSentinels.add(RedisHostAndPort.of(host, port));
            return this;
        }

        public static RedisConnectionConfig.Builder redisCluster(String host){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisSentinel(host);
        }

        public static RedisConnectionConfig.Builder redisCluster(String host, int port){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));
            
            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisCluster(host, port);
        }

        public RedisConnectionConfig.Builder withRedisCluster(String host){
            return withRedisCluster(host, DEFAULT_CLUSTER_PORT);
        }

        public RedisConnectionConfig.Builder withRedisCluster(String host, int port) {
            Preconditions.checkArgument(StringUtils.isNotBlank(host), HOST_MUST_NOT_BE_EMPTY);
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));
            redisClusters.add(RedisHostAndPort.of(host, port));
            return this;
        }

        public RedisConnectionConfig.Builder withHost(String host){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), "host must not be empty");
            this.host = host;
            return this;
        }

        public RedisConnectionConfig.Builder withPort(int port){
            Preconditions.checkArgument(StringUtils.isNotBlank(host), "host must not be empty");
            Preconditions.checkArgument(isValidPort(port), String.format(PORT_OUT_OF_RANGE, port));

            this.port = port;
            return this;
        }

        public RedisConnectionConfig.Builder withDatabase(int database){
            Preconditions.checkArgument(database >= 0, "Invalid database number: " + database);
            this.database = database;
            return this;
        }

        public RedisConnectionConfig.Builder withClientName(String clientName){
            Preconditions.checkArgument(StringUtils.isNotBlank(clientName), "Clientname must not be null ");

            this.clientName = clientName;
            return this;
        }

        public RedisConnectionConfig.Builder withPassword(String password){
            Preconditions.checkArgument(password != null, "Password must not be null");

            return withPassword(password.toCharArray());
        }

        public Builder withPassword(char[] password) {
            Preconditions.checkArgument(password != null, "Password must not be null");

            this.password = Arrays.copyOf(password, password.length);
            return this;
        }

        public RedisConnectionConfig.Builder withTimeout(long timeout){
            Preconditions.checkArgument(timeout >= 0, "Timeout must be greater or equal 0");

            this.timeout = timeout;
            return this;
        }

        public RedisConnectionConfig.Builder withSentinelMasterId(String sentinelMasterId){
            Preconditions.checkArgument(StringUtils.isNotBlank(sentinelMasterId), "Sentinel master id must not empty");
            this.redisSentinelMasterId = sentinelMasterId;
            return this;
        }

        public RedisConnectionConfig build() {
            if (redisSentinels.isEmpty() && redisClusters.isEmpty() && StringUtils.isEmpty(host)) {
                throw new IllegalStateException(
                        "Cannot build a RedisConnectionConfig. One of the following must be provided Host, Socket, Cluster or "
                                + "Sentinel");
            }
            RedisConnectionConfig redisConnectionConfig = new RedisConnectionConfig();
            redisConnectionConfig.setHost(host);
            redisConnectionConfig.setPort(port);

            if(password != null){
                redisConnectionConfig.setPassword(password);
            }
            redisConnectionConfig.setDatabase(database);
            redisConnectionConfig.setClientName(clientName);
            redisConnectionConfig.setRedisSentinelMasterId(redisSentinelMasterId);
            for(RedisHostAndPort sentinel : redisSentinels){
                redisConnectionConfig.getRedisSentinels().add(new RedisConnectionConfig(sentinel.getHost(),sentinel.getPort(), timeout));
            }
            for(RedisHostAndPort cluster: redisClusters){
                redisConnectionConfig.getRedisClusters().add(new RedisConnectionConfig(cluster.getHost(), cluster.getPort(), timeout));
            }
            redisConnectionConfig.setTimeout(timeout);
            return redisConnectionConfig;
        }
    }
}
