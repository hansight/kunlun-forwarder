package com.hansight.kunlun.forwarder.collector.forwarder.parser;


import java.util.Map;

import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.forwarder.lexer.CEFLogLexer;

/**
 * @author yzhhui
 */
public class CEFLogParser extends DefaultLogParser {
    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        return new CEFLogLexer();
    }
}