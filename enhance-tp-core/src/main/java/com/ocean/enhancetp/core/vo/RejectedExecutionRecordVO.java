package com.ocean.enhancetp.core.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/18
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
public class RejectedExecutionRecordVO {
    /**
     * 任务类型
     */
    private String runnableClassName;
}
