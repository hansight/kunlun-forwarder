package com.hansight.kunlun.forwarder.collector.forwarder.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.exception.CollectorException;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.forwarder.lexer.DefaultLexer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yzhhui
 */
public class JSONLogParser extends DefaultLogParser {
    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {

        return new DefaultLexer<Event, Map<String, Object>>() {

            @Override
            public Map<String, Object> parse(Event event) throws CollectorException {
                String value = new String(event.getBody().array());
                Map<String, Object> log;
                try {
                    log = JSON.parseObject(value);
                } catch (JSONException e) {
                    log = new LinkedHashMap<>();
                    log.put("data", value);
                    log.put("error", e);
                    logger.debug("event parser error,data:{}", value);
                }
                return log;
            }
        };
    }
}