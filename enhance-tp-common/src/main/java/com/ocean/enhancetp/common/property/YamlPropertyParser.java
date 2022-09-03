package com.ocean.enhancetp.common.property;

import cn.hutool.setting.yaml.YamlUtil;
import com.ocean.enhancetp.common.spi.SpiOrder;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/24
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@SpiOrder
public class YamlPropertyParser implements PropertyParser {

    @Override
    public Set<String> includeExtNames(){
        Set<String> includeExtNames = new HashSet<>();
        includeExtNames.add("yml");
        includeExtNames.add("yaml");
        return includeExtNames;
    }

    @Override
    public <T> T parseObject(String content, Class<T> clazz) {
        return YamlUtil.load(new StringReader(content), clazz);
    }
}
