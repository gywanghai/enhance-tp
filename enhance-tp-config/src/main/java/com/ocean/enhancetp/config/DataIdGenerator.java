package com.ocean.enhancetp.config;

/**
 * @description: 数据ID产生器
 * @author：二师兄，微信：happy_coder
 * @date: 2022/9/2
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public interface DataIdGenerator {

    public String generator(String application, String profile, String originId);

}
