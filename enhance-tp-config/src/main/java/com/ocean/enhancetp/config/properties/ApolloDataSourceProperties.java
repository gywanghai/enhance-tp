package com.ocean.enhancetp.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Getter
@Setter
@ToString
public class ApolloDataSourceProperties extends AbstractDataSourceProperties {

    private String appId;

    private String namespace;

    private String meta;

    private String dataId;

    private String defaultValue;

    @Override
    public void preCheck() {
        // ignore
    }
}
