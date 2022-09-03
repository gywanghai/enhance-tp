package com.ocean.enhancetp.config.properties;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Setter
@Getter
@ToString
public class ConsulDataSourceProperties extends AbstractDataSourceProperties {

    private String host;

    private Integer port = 8500;

    private String dataId;

    private int waitTimeoutInSecond = 1;

    @Override
    public void preCheck() {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "ConsulDataSource server-host is empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(dataId), "ConsulDataSource dataId can not be empty");
    }
}
