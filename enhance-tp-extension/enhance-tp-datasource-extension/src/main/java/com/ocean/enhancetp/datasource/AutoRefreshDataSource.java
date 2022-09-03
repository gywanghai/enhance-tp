package com.ocean.enhancetp.datasource;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 支持自动刷新的数据源
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public abstract class AutoRefreshDataSource<S, T> extends AbstractDataSource<S, T> {
    /**
     * 定时任务线程池服务
     */
    private ScheduledExecutorService service;
    /**
     * 自动刷新间隔
     */
    protected long recommendRefreshMs = 3000;

    protected AutoRefreshDataSource(Converter<S, T> parser) {
        super(parser);
        startTimerService();
    }

    protected AutoRefreshDataSource(Converter<S, T> parser, long recommendRefreshMs){
        super(parser);
        if(recommendRefreshMs <= 0){
            throw new IllegalArgumentException("recommendRefreshMs must > 0, but " + recommendRefreshMs + "get");
        }
        this.recommendRefreshMs = recommendRefreshMs;
    }

    private void startTimerService() {
        service = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("enhancetp-datasource-auto-refresh-task", true));
        service.scheduleAtFixedRate(() -> {
            Set<String> dataIds = propertyMap.keySet();
            dataIds.stream().forEach(dataId -> {
                if(!isModified(dataId)){
                    return;
                }
                try {
                    T newValue = loadConfig(dataId);
                    getProperty(dataId).updateValue(newValue);
                } catch (Exception e) {
                    log.error("loadConfig exception", e);
                }
            });
        }, recommendRefreshMs, recommendRefreshMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        if(service != null){
            service.shutdown();
            service = null;
        }
    }

    protected boolean isModified(String dataId){
        return true;
    }
}
