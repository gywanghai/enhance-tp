package com.ocean.enhancetp.config.datasource;

import com.ctrip.framework.apollo.ConfigService;
import com.ocean.enhancetp.autoconfig.EnhanceTpProperties;
import com.ocean.enhancetp.autoconfig.configuration.EnhanceTpEnableAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/9/1
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@ConditionalOnClass(ConfigService.class)
@Import({ApolloDataSourceConfiguration.class, EnhanceTpProperties.class})
@AutoConfigureAfter(EnhanceTpEnableAutoConfiguration.class)
public class ApolloDataSourceAutoConfiguration {

}
