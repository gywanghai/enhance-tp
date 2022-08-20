package com.ocean.enhancetp.core.event;

/**
 * @description: 事件源枚举
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/14
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public enum EventSource {
    /**
     * 任务被拒绝
     */
    ALARM(1, "线程池告警");

    private int code;

    private String info;

    EventSource(int code, String info){
        this.code = code;
        this.info = info;
    }

//    public String getInfo() {
//        return info;
//    }

    public int getCode() {
        return code;
    }
}
