package com.ocean.enhancetp.starter;

import com.ocean.enhancetp.core.wrapper.ThreadPoolExecutorWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/23
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class ThreadPoolExecutorWrapperFactoryBean implements FactoryBean<ThreadPoolExecutorWrapper>, BeanFactoryAware {

    private BeanFactory beanFactory;
    /**
     * 线程池对应的 BeanName
     */
    private String threadPoolExecutorBeanName;
    /**
     * 线程池ID
     */
    private String threadPoolId;

    public ThreadPoolExecutorWrapperFactoryBean(String threadPoolExecutorBeanName, String threadPoolId){
        this.threadPoolExecutorBeanName = threadPoolExecutorBeanName;
        this.threadPoolId = threadPoolId;
    }

    @Override
    public ThreadPoolExecutorWrapper getObject() throws Exception {
        return new ThreadPoolExecutorWrapper((ThreadPoolExecutor) beanFactory.getBean(threadPoolExecutorBeanName), threadPoolId);
    }

    @Override
    public Class<ThreadPoolExecutorWrapper> getObjectType() {
        return ThreadPoolExecutorWrapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
