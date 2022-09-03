package com.ocean.enhancetp.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: ZooKeep 数据源属性
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Setter
@Getter
@ToString
public class ZookeeperDatasourceProperties extends AbstractDataSourceProperties {

    private String serverAddr = "localhost:2181";

    private String path;

    private String groupId;

    private String dataId;

    @Override
    public void preCheck() {
        if(StringUtils.isBlank(serverAddr)){
            serverAddr = this.getEnv().getProperty("spring.cloud.enhancetp.zk.server-addr", "");
            if(StringUtils.isBlank(serverAddr)){
                throw new IllegalArgumentException("ZookeeperDataSource server-addr is empty");
            }
        }
    }
}
