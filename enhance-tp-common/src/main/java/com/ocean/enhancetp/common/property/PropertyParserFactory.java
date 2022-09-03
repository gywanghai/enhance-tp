package com.ocean.enhancetp.common.property;

import cn.hutool.core.util.ServiceLoaderUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/24
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class PropertyParserFactory {

    private PropertyParserFactory(){}

    private static final Map<String, PropertyParser> propertyParserMap = new ConcurrentHashMap<>();

    static {
        ServiceLoaderUtil.loadList(PropertyParser.class).stream().forEach(propertyParser -> {
            Set<String> includeExtNames = propertyParser.includeExtNames();
            includeExtNames.stream().forEach(includeExtName -> propertyParserMap.put(includeExtName, propertyParser));
        });
    }

    public static final PropertyParser getPropertyParser(String extName){
        return propertyParserMap.get(extName);
    }
}
