package com.hansight.kunlun.forwarder.collector.common.base;

import java.util.Queue;

import com.hansight.kunlun.forwarder.collector.common.exception.CollectorException;
import com.hansight.kunlun.forwarder.collector.coordinator.metric.MetricService;

/**
 * Author:zhhui
 * DateTime:2014/7/23 15:23.
 */
public interface Lexer<F, T> extends Runnable {
    String name();

    void set(String name);

    /**
     * parse take time is long??
     *
     * @throws com.hansight.kunlun.collector.common.exception.CollectorException
     */
    T parse(F from) throws CollectorException;

    /**
     * 设置要解析的值
     *
     * @param values @see Queue
     */
    void setValueToParse(Queue<F> values);

    /**
     * 解析结果存储器
     *
     * @param writer @see LogWriter
     */
    void setWriter(LogWriter<T> writer);

    /**
     * 监控服务
     *
     * @param metricService @see   MetricService
     */
    void setMetric(MetricService metricService);

    boolean isDone();

    void start();
}
