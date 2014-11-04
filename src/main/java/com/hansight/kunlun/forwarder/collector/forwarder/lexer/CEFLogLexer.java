package com.hansight.kunlun.forwarder.collector.forwarder.lexer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.forwarder.collector.common.exception.CollectorException;
import com.hansight.kunlun.forwarder.collector.common.model.Event;

import java.util.LinkedHashMap;
import java.util.Map;

public class CEFLogLexer extends DefaultLexer<Event, Map<String, Object>> {
    protected final static Logger logger = LoggerFactory.getLogger(CEFLogLexer.class);
    private String separate = "\\|";
    private String separateWithSpace = " \\| ";
    private int cef = "CEF:".length();
    private String[] fields = "version|device_vendor|device_product|device_version|signature_id|name|severity|extension".split("\\|");

//    public SyslogCEFLogLexer(String[] fields) {
//        this(fields, "[ ,\t\r\n\f]");
//    }
//
//    public SyslogCEFLogLexer(String[] fields, String separate) {
//        //    this.threadPool = threadPool;
//        this.fields = fields;
//        this.separate = separate;
//        this.separateWithSpace = " " + separate + " ";
//    }

    /**
     * check each value of the values is end with escaped char: '\',then merge with the next one
     *
     * @param values   from
     * @param separate
     * @param length
     * @param trim     need esc space
     * @return to
     */
    private String[] checkEsc(String[] values, String separate, int length, boolean trim) {

        int j = 0;
        String value;
        for (int i = 0; i < values.length; i++) {
            if ((i + j) < values.length && values[i + j] != null) {
                if (trim)
                    value = values[i + j].trim();
                else
                    value = values[i + j];

                while ((i + j+1) < values.length && value.endsWith("\\")) {
                    j++;
                    if (trim)
                        value += (separate + values[i + j].trim());
                    else
                        value += (separate + values[i + j]);
                }
            } else {
                value = "";
            }
            values[i] = value;
        }
        for (int i = length; i < values.length; i++) {


            values[length - 1] = values[length - 1] + separate + values[i];
        }
        int len = values.length - j;
        len = len >= length ? length : len;
        String[] newValues = new String[len];
        System.arraycopy(values, 0, newValues, 0, newValues.length);

        return newValues;
    }

    /**
     * from second value, check each value of the values is contain char: '=' and not contain char: '\=',then merge this to  the before one
     *
     * @param values
     * @param separate
     * @return
     */
    private String[] checkEquals(String[] values, String separate) {
        int len = 0;
        int j = 0;
        String value;
        for (int i = 0; i < values.length; i++) {

            if ((i + j) < values.length && values[i + j] != null) {
                value = values[i + j];
                while ((i + j + 1) < values.length && !(values[i + j + 1].contains("=") && !values[i + j + 1].contains("\\="))) {
                    j++;
                    value += (separate + values[i + j]);
                }
            } else {
                value = "";
            }
            values[i] = value;
        }
        String[] newValues = new String[values.length - j];
        System.arraycopy(values, 0, newValues, 0, newValues.length);

        return newValues;
    }

    @Override
    public Map<String, Object> parse(Event event) throws CollectorException {
        String line;
        line = new String(event.getBody().array()).replaceAll(separate, separateWithSpace);

        return parse(line);
    }

    public Map<String, Object> parse(String line) {
        Map<String, Object> log = new LinkedHashMap<>();
        String[] values = line.split(separate);
        //   logger.debug("encoding{}", encoding);
        logger.debug("fields:length{},{}", fields.length, fields);
        logger.debug("values:length{},{}", values.length, values);
        values = checkEsc(values, separate, fields.length, true);

        if (values.length == 8) {
            log.put(fields[0], values[0].trim().substring(cef));
            for (int i = 1; i < 7; i++) {
                if (values[i] != null) {
                    String value = values[i].trim();
                    if ("".equals(value)) {
                        continue;
                    }
                    log.put(fields[i], value);
                }

            }
            if (values[7] != null) {
                String[] f7s = values[7].split(" ");
                f7s = checkEsc(f7s, " ", f7s.length, false);
                f7s = checkEquals(f7s, " ");
                for (String kv : f7s) {
                    String[] kvs = kv.split("=", 2);
                    if (kvs[0] == null || "".equals(kvs[0]) || kvs[1] == null || "".equals(kvs[1])) {
                        continue;
                    }
                    log.put(kvs[0], kvs[1]);
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