package com.ocean.enhancetp.example;

import com.ocean.enhancetp.starter.EnhanceThreadPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/21
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@SpringBootApplication
@EnableScheduling
public class ExampleApplication {

    @EnhanceThreadPool(threadPoolId = "test")
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(1, 1, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @EnhanceThreadPool(threadPoolId = "test2")
    @Bean
    public ThreadPoolExecutor threadPoolExecutor2(){
        return new ThreadPoolExecutor(5, 6, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
