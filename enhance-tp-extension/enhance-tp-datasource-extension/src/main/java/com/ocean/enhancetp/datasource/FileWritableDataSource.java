package com.ocean.enhancetp.datasource;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description: 文件可写数据源
 * @author：二师兄，微信：happy_coder
 * @date: 2022/8/28
 * @Copyright： 公众号：海哥聊架构 | 博客：https://gywanghai.github.io/technote/ - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class FileWritableDataSource<T> implements WritableDataSource<T>{

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private Converter<T, String> configEncoder;

    private File file;

    private Charset charset;

    private Lock lock = new ReentrantLock(true);

    public FileWritableDataSource(String filePath, Converter<T, String> configEncoder){
        this(new File(filePath), configEncoder);
    }

    public FileWritableDataSource(File file, Converter<T, String> configEncoder){
        this(file, configEncoder, DEFAULT_CHARSET);
    }

    public FileWritableDataSource(File file, Converter<T, String> configEncoder, Charset charset){
        if(file == null || file.isDirectory()){
            throw new IllegalArgumentException("Bad file");
        }
        if(configEncoder == null){
            throw new IllegalArgumentException("Config encoder can not be null");
        }
        if(charset == null){
            throw new IllegalArgumentException("Charset can not be null");
        }
        this.configEncoder = configEncoder;
        this.file = file;
        this.charset = charset;
    }

    @Override
    public void write(T value) {
        lock.lock();
        try{
            String convertResult = configEncoder.convert(value);
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] bytesArray = convertResult.getBytes(charset);

                log.info("[FileWritableDataSource] Writing to file {}: {}", file, convertResult);
                fileOutputStream.write(bytesArray);
                fileOutputStream.flush();
            } catch (FileNotFoundException e) {
                log.error("[FileWritableDataSource] Writing to file {} not exists", file.getAbsoluteFile());
            } catch (IOException e) {
                log.error("[FileWritableDataSource] Error when writing to file {}: {}", file, convertResult);
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        // ignore
    }
}
