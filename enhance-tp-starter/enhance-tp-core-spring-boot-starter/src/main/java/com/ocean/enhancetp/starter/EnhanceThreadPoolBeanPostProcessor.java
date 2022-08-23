package com.ocean.enhancetp.starter;

import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/23
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class EnhanceThreadPoolBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ThreadPoolExecutor){
            EnhanceThreadPool enhanceThreadPool = applicationContext.findAnnotationOnBean(beanName, EnhanceThreadPool.class);
            if(enhanceThreadPool != null){
                applicationContext.getBean(beanName + "Wrapper", ThreadPoolExecutorWrapper.class);
            }
        }
        return bean;
    }
}
