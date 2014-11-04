package com.hansight.kunlun.forwarder.collector.forwarder.lexer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.forwarder.collector.common.exception.CollectorException;
import com.hansight.kunlun.forwarder.collector.common.model.Event;

import java.util.LinkedHashMap;
import java.util.Map;

public class DelimiterLogLexer extends DefaultLexer<Event, Map<String, Object>> {
    protected final static Logger logger = LoggerFactory.getLogger(DelimiterLogLexer.class);
    private String separate;
    private String separateWithSpace;
    private String[] fields;

    public DelimiterLogLexer(String[] fields) {
        this(fields, "[ ,\t\r\n\f]");
    }

    public DelimiterLogLexer(String[] fields, String separate) {
        //    this.threadPool = threadPool;
        this.fields = fields;
        this.separate = separate;
        this.separateWithSpace = " " + separate + " ";
    }

    @Override
    public Map<String, Object> parse(Event event) throws CollectorException {
        Map<String, Object> log = new LinkedHashMap<>();
        String line;
        line = new String(event.getBody().array()).replaceAll(separate, separateWithSpace);
             /*   if (encoding == null || "".equals(encoding)) {

                } else {
                    line = new String(event.getBody().array(), encoding);
                }*/

        String[] values = line.split(separate);
        //   logger.debug("encoding{}", encoding);
        logger.debug("fields:length{},{}", fields.length, fields);
        logger.debug("values:length{},{}", values.length, values);
        if (fields.length == values.length) {
            for (int i = 0; i < fields.length; i++) {
                if (values[i] != null) {
                    String value = values[i].trim();
                    if ("".equals(value)) {
                        continue;
                    }
                    int len = value.length();
                    if (len >= 1) {
                        char c = value.charAt(0);
                        if (c == '"' || c == '\'') {
                            value = value.substring(1);
                        }
                    }
                    len = value.length();
                    if (len >= 1) {
                        char c = value.charAt(len - 1);
                        if (c == '"' || c == '\'') {
                            value = value.substring(0, len - 1);
                        }
                    }
                    log.put(fields[i], value);
                }

            }
        } else {
            log.put("message", line);
            log.put("error", "your set fields.length:" +
                    fields.length + ",not match the values.length:" + values.length + "that you want parser");
            //   throw new CollectorException("your set fields,not match the values that you want parser");
        }

         /*   try {

            } catch (UnsupportedEncodingException e) {
                log.put("error", "your set UnsupportedEncoding you set for");
                throw new CollectorException("your set UnsupportedEncoding you set for", e);
            }*/

        return log;
    }
}