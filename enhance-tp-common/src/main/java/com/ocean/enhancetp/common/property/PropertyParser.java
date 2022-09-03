package com.ocean.enhancetp.common.property;

import java.util.Set;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/24
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface PropertyParser {
    /**
     * 对那些文件后缀名感兴趣
     * @return
     */
    public Set<String> includeExtNames();
    /**
     * 将字符串解析为对象
     * @param content
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T parseObject(String content, Class<T> clazz);
}
