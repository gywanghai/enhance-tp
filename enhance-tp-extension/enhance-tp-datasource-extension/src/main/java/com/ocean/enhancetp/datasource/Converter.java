package com.ocean.enhancetp.datasource;

/**
 * 对象转换器——将一个对象从 Source 类型 转换为 Type 类型
 * @param <S>
 * @param <T>
 */
public interface Converter<S, T> {

    T convert(S source);
}
