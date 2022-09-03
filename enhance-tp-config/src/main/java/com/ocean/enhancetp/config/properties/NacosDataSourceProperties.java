package com.ocean.enhancetp.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: Nacos 数据源属性
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Getter
@Setter
@ToString
public class NacosDataSourceProperties extends AbstractDataSourceProperties {

    private String serverAddr;

    private String username;

    private String password;

    private String groupId = "DEFAULT_GROUP";

    private String dataId;

    private String namespace;

    private String accessKey;

    private String secretKey;

    @Override
    public void preCheck() {
        if(StringUtils.isBlank(serverAddr)){
            serverAddr = this.getEnv().getProperty("enhancetp.datasource.nacos.server-addr", "localhost:8848");
        }
    }

}
