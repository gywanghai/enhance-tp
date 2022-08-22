package com.ocean.enhancetp.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @description: 任务等待时间记录值对象，接近于队列排队时间
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/22
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Data
@ToString
@AllArgsConstructor
public class WaitTimeRecordVO {

    private String runnableClassName;

    private Long time;

}
