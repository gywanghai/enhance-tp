package com.ocean.enhancetp.starter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Configuration
public class EnhanceTpCoreAutoConfiguration {

    @Bean
    public String testString(){
        return "testString";
    }
}
