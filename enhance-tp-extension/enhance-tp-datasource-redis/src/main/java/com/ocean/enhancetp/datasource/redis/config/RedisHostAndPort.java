package com.ocean.enhancetp.datasource.redis.config;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static cn.hutool.core.net.NetUtil.isValidPort;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/29
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class RedisHostAndPort {

    private static final int NO_PORT = -1;

    private String host;

    private int port;

    private RedisHostAndPort(String host, int port){
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "host must not be empty");
        this.host = host;
        this.port = port;
    }

    public static RedisHostAndPort of(String host, int port) {
        Preconditions.checkArgument(isValidPort(port), "Port out of range: %s", port);
        return new RedisHostAndPort(host, port);
    }

    public boolean hasPort(){
        return port != NO_PORT;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        if(!hasPort()){
            throw new IllegalStateException("No port present");
        }
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RedisHostAndPort that = (RedisHostAndPort) o;

        return new EqualsBuilder().append(port, that.port).append(host, that.host).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(host).append(port).toHashCode();
    }

    @Override
    public String toString() {
        return "RedisHostAndPort{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
