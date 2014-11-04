package com.hansight.kunlun.forwarder.collector.forwarder.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.forwarder.lexer.DelimiterLogLexer;

import java.util.Map;

/**
 * @author tao_zhang
 */
public class DelimiterLogParser extends DefaultLogParser {


    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        Lexer<Event, Map<String, Object>> lexer;
        String pattern = conf.get("pattern");
        logger.debug("pattern:{}", pattern);
        JSONObject obj = JSON.parseObject(pattern);
        JSONArray array = obj.getJSONArray("fields");
        String[] fields = new String[array.size()];
        array.toArray(fields);
        String separate = obj.getString("separate");
        if (separate != null) {
            lexer = new DelimiterLogLexer(fields, separate);
        } else {
            lexer = new DelimiterLogLexer(fields);
        }
        return lexer;
    }
}