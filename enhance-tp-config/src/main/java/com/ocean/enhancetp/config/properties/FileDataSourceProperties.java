package com.ocean.enhancetp.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/27
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Getter
@Setter
@ToString
public class FileDataSourceProperties extends AbstractDataSourceProperties {

    private String file;

    private Charset charset = StandardCharsets.UTF_8;

    private long recommendRefreshMs = 3000L;

    private int bufSize = 1024 * 1024;

    @Override
    public void preCheck() {
        try {
            this.setFile(ResourceUtils.getFile(StringUtils.trimAllWhitespace(this.getFile())).getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("[EnhanceTp Starter] DataSource handle file [" + this.getFile() + "] error :" + e.getMessage());
        }
    }
}
