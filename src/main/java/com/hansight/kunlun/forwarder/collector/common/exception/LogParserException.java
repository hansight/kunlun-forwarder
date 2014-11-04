package com.hansight.kunlun.forwarder.collector.common.exception;

/**
 * Author:zhhui
 * DateTime:2014/7/30 9:50.
 */
public class LogParserException extends CollectorException {
    public LogParserException(String message) {
        super(message);
    }

    public LogParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
