package com.hansight.kunlun.forwarder.collector.forwarder.lexer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.base.LogWriter;
import com.hansight.kunlun.forwarder.collector.coordinator.metric.MetricException;
import com.hansight.kunlun.forwarder.collector.coordinator.metric.MetricService;
import com.hansight.kunlun.forwarder.collector.coordinator.metric.WorkerStatus;
import java.util.Map;
import java.util.Queue;

/**
 * Author:zhhui
 * DateTime:2014/7/28 13:04.
 */
public abstract class DefaultLexer<F, T extends Map<String, Object>> implements Lexer<F, T> {
    private String name;
    protected final static Logger logger = LoggerFactory.getLogger(DefaultLexer.class);
    protected LogWriter<T> writer;
    Queue<F> events;
    MetricService metricService;
    private boolean isDone = false;
    private static boolean markStatus = false;

    public void setMetric(MetricService metricService) {
        this.metricService = metricService;
    }

    @Override
    public void run() {
        isDone = false;
        long times = 0;
        if (logger.isInfoEnabled())
            times = System.currentTimeMillis();
        int caches = events.size();
        F f;
        while ((f = events.poll()) != null) {
            //    metricService.mark();
            try {
                T log = parse(f);
                writer.write(log);
            } catch (Exception e) {
                logger.error("lexer error :{}", e);
                try {
                    metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
                } catch (MetricException e1) {
                    logger.error("Metric error :{}", e1);
                }
            }

        }
        logger.info("parse {}, take {} ms", caches, System.currentTimeMillis() - times);
        try {
            events.clear();
            writer.close();
            if (!markStatus) {
                markStatus = true;
                metricService.setProcessorStatus(WorkerStatus.ConfigStatus.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("lexer error :{}", e);
            try {
                metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
            } catch (MetricException e1) {
                logger.error("Metric error :{}", e1);
            }
        }
        isDone = true;
    }

    @Override
    public void setValueToParse(Queue<F> events) {
        this.events = events;
    }

    @Override
    public void setWriter(LogWriter<T> writer) {
        this.writer = writer;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void start() {
        isDone = false;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void set(String name) {
        this.name = name;
    }
}
