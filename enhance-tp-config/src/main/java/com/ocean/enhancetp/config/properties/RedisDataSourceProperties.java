package com.ocean.enhancetp.config.properties;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@ToString
@Getter
@Setter
public class RedisDataSourceProperties extends AbstractDataSourceProperties {

    private String host = "localhost";

    private int port = 6379;

    private int database;

    private long timeout;

    private List<String> nodes;

    private String dataId;

    private String channel;

    private String masterId;

    @Override
    public void preCheck() {
        Preconditions.checkArgument(StringUtils.isNotBlank(dataId), "RedisDataSource dataId can not be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(channel), "RedisDataSource channel can not be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(masterId), "RedisDataSource sentinel mode masterId can not be empty");
    }
}
