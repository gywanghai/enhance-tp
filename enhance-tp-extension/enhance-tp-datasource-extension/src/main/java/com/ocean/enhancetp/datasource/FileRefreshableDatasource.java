package com.ocean.enhancetp.datasource;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @description: 文件可刷新数据源
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class FileRefreshableDatasource<T> extends AutoRefreshDataSource<String, T>{

    private static final int MAX_SIZE = 1024 * 1024 * 4;

    private static final long DEFAULT_REFRESH_MS = 3000;

    private static final int DEFAULT_BUF_SIZE = 1024 * 1024;

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private byte[] buf;

    private Charset charset;

    private File file;

    private long lastModified = 0L;

    public FileRefreshableDatasource(File file, Converter<String, T> configParser) {
        this(file, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHARSET);
    }

    public FileRefreshableDatasource(String fileName, Converter<String, T> configParser){
        this(new File(fileName), configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHARSET);
    }

    public FileRefreshableDatasource(File file, Converter<String, T> configParser, int bufSize){
        this(file, configParser, DEFAULT_REFRESH_MS, bufSize, DEFAULT_CHARSET);
    }

    public FileRefreshableDatasource(File file, Converter<String, T> configParser, Charset charset){
        this(file, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, charset);
    }

    public FileRefreshableDatasource(File file, Converter<String, T> configParser, long recommendRefreshMs, int bufSize, Charset charset){
        super(configParser, recommendRefreshMs);
        if(bufSize <= 0 || bufSize > MAX_SIZE){
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }
        if(file == null || file.isDirectory()){
            throw new IllegalArgumentException("file can not be null or a directory");
        }
        if(charset == null){
            throw new IllegalArgumentException("charset can not be null");
        }
        this.buf = new byte[bufSize];
        this.file = file;
        this.charset = charset;
        this.lastModified = file.lastModified();
        firstLoad();
    }

    private void firstLoad() {
//        try {
//            T newValue = loadConfig();
//            getProperty().updateValue(newValue);
//        } catch (Exception e) {
//            log.error("[FileInJarReadableDatasource] error when loading config", e);
//        }
    }

    @Override
    public String readSource(String dataId) {
        String content = null;
        if(!file.exists()){
            log.warn("[FileRefreshableDataSource] File does not exist : {}", file.getAbsoluteFile());
        }
        try (FileInputStream inputStream = new FileInputStream(file)) {
            FileChannel fileChannel = inputStream.getChannel();
            if (fileChannel.size() > buf.length) {
                throw new IllegalStateException(file.getAbsolutePath() + " file size = " + fileChannel.size() + ", is bigger than bufferSize = " + buf.length + ". Can't read");
            }
            int len = inputStream.read(buf);
            content = new String(buf, 0, len, charset);
        } catch (FileNotFoundException e) {
            log.warn("[FileRefreshableDataSource] File does not exist : {}", file.getAbsoluteFile());
        } catch (IOException e) {
            log.error("[FileRefreshableDataSource] error when load config from datasource", e);
        }
        return content;
    }

    @Override
    protected boolean isModified(String dataId) {
        long curLastModified = file.lastModified();
        if(curLastModified != this.lastModified){
            this.lastModified = curLastModified;
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        super.close();
        buf = null;
    }
}
