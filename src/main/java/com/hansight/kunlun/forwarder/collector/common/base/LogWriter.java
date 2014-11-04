package com.hansight.kunlun.forwarder.collector.common.base;

import java.io.Closeable;
import java.io.Flushable;

import com.hansight.kunlun.forwarder.collector.common.exception.LogWriteException;

public interface LogWriter<T> extends Flushable, Closeable {
    void write(T t) throws LogWriteException;
}
