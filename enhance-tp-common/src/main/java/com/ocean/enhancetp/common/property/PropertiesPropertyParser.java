package com.ocean.enhancetp.common.property;

import cn.hutool.json.JSONUtil;
import com.ocean.enhancetp.common.spi.SpiOrder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/24
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
@SpiOrder
public class PropertiesPropertyParser implements PropertyParser{

    @Override
    public Set<String> includeExtNames() {
        Set<String> includeExtNames = new HashSet<>();
        includeExtNames.add("properties");
        return includeExtNames;
    }

    @Override
    public <T> T parseObject(String content, Class<T> clazz) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            log.error("load properties fail ", e);
        }
        return JSONUtil.toBean(content, clazz);
    }

}
