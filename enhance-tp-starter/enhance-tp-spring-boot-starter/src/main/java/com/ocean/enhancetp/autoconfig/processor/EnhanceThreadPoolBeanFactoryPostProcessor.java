package com.ocean.enhancetp.autoconfig.processor;

import com.ocean.enhancetp.autoconfig.annotation.EnhanceThreadPool;
import com.ocean.enhancetp.autoconfig.factorybean.ThreadPoolExecutorWrapperFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Arrays;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class EnhanceThreadPoolBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private BeanDefinitionRegistry beanDefinitionRegistry;

    /**
     * * BeanDefinitionRegistry 可以直接注册 BeanDefinition
     * @param registry the bean definition registry used by the application context
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.beanDefinitionRegistry = registry;
    }

    /**
     * ConfigurableListableBeanFactory 只能把已创建好的对象注册到 Spring IoC 容器中
     * @param configurableListableBeanFactory the bean factory used by the application context
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        String[] beanNames = configurableListableBeanFactory.getBeanNamesForAnnotation(EnhanceThreadPool.class);
        if(beanNames.length == 0){
            return;
        }
        Arrays.stream(beanNames).forEach(beanName -> {
            String wrapperBeanDefinitionName = StringUtils.joinWith("", beanName, "Wrapper");
            EnhanceThreadPool enhanceThreadPool = configurableListableBeanFactory.findAnnotationOnBean(beanName, EnhanceThreadPool.class);
            if(enhanceThreadPool == null){
                return;
            }
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition) BeanDefinitionBuilder.genericBeanDefinition(ThreadPoolExecutorWrapperFactoryBean.class).getBeanDefinition();
            beanDefinition.setAbstract(false);
            beanDefinition.setAutowireCandidate(true);
            beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
            beanDefinition.setLazyInit(false);
            // 线程池 Bean 的名称
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, beanName);
            // 线程池ID
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, enhanceThreadPool.threadPoolId());
            // 注册 Thread
            beanDefinitionRegistry.registerBeanDefinition(wrapperBeanDefinitionName, beanDefinition);
        });
    }
}
