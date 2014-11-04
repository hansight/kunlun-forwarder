package com.hansight.kunlun.forwarder.collector.forwarder.parser;

import java.util.Map;
import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.forwarder.lexer.XMLLogLexer;

/**
 * @author tao_zhang
 */
public class XMLLogParser extends DefaultLogParser {

    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        return new XMLLogLexer(this.conf.get("encoding"));
    }
}