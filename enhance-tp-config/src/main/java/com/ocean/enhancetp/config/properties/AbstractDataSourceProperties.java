package com.ocean.enhancetp.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.env.Environment;

/**
 * @description: 抽象数据源属性
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Getter
@Setter
@ToString
public abstract class AbstractDataSourceProperties {
    /**
     * 数据格式
     */
    private String dataType = "json";
    /**
     * 对象转换器类名
     */
    private String converterClass;

    private Environment env;
    /**
     * 前置检查
     */
    public abstract void preCheck();

    public Environment getEnv() {
        return env;
    }
}
