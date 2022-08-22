package com.ocean.enhancetp.core.alarm;

/**
 * @description: 告警类型枚举
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/11
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public enum AlarmType {
    /**
     * 线程池配置信息更改
     */
    CONFIG_UPDATE(10, "线程池配置信息更改"),
    /**
     * 线程池活跃度
     */
    THREADPOOL_LIVENESS(20, "线程池活跃度"),
    /**
     * 阻塞等待队列
     */
    BLOCKING_QUEUE_SIZE(30, "阻塞等待队列"),
    /**
     * 任务执行超时
     */
    TASK_EXECUTION_TIMEOUT(40, "任务执行超时"),
    /**
     * 任务被拒绝
     */
    TASK_REJECTED(50, "任务被拒绝"),
    /**
     * 任务执行失败
     */
    TASK_FAIL(60, "任务执行失败"),
    /**
     * 任务等待超时
     */
    WAIT_TIMEOUT(70, "任务等待超时");

    private int code;

    private String info;

    AlarmType(int code, String info){
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "AlarmType{" +
                "code=" + code +
                ", info='" + info + '\'' +
                '}';
    }
}
