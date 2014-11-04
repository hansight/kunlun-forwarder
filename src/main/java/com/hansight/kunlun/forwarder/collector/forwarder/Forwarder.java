package com.hansight.kunlun.forwarder.collector.forwarder;


import com.hansight.kunlun.forwarder.collector.common.base.LogParser;
import com.hansight.kunlun.forwarder.collector.common.utils.ForwarderConstants;
import com.hansight.kunlun.forwarder.collector.coordinator.config.ForwarderConfig;
import com.hansight.kunlun.forwarder.collector.coordinator.config.ForwarderMonitorService;
import com.hansight.kunlun.forwarder.collector.coordinator.config.MonitorException;
import com.hansight.kunlun.forwarder.collector.coordinator.config.MonitorService;
import com.hansight.kunlun.forwarder.collector.coordinator.config.Config;
import com.hansight.kunlun.forwarder.collector.forwarder.parser.RegexLogParser;


import com.hansight.kunlun.forwarder.collector.utils.Common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Forwarder {

    public final static Logger logger = LoggerFactory.getLogger(Forwarder.class);
    private static ExecutorService threadPool;
    private static Map<String, LogParser> parsers = new ConcurrentHashMap<>();
    private static String forwarder;
    public final static Properties CLASS_MAPPING = new Properties();
    public final static Properties GLOBAL = new Properties();

    static {
        InputStream global = Forwarder.class.getClassLoader()
                .getResourceAsStream(ForwarderConstants.FORWARDER_CONF);
        InputStream class_mapping = ForwarderConfig.class.getClassLoader()
                .getResourceAsStream(ForwarderConstants.CLASS_MAPPING_CONF);
        try {
            GLOBAL.load(global);
            GLOBAL.putAll(Common.getAll());
            CLASS_MAPPING.load(class_mapping);
        } catch (IOException e) {
            logger.error("forward initialize error:{}", e);
        }
    }

    public static void main(String[] args) throws IOException {
        threadPool = Executors.newCachedThreadPool();
        forwarder = GLOBAL.getProperty(ForwarderConstants.FORWARDER_ID, ForwarderConstants.FORWARDER_ID_DEFAULT);
        MonitorService<ForwarderConfig> service = new ForwarderMonitorService(forwarder);
        try {
            service.registerConfigChangedProcessor(new ConfigChangedAction());
            threadPool.submit(service);
        } catch (MonitorException e) {
            logger.error("monitor error:{}", e);
        }
    }

    private static class ConfigChangedAction implements
            MonitorService.ConfigChangedProcessor<ForwarderConfig> {
        @Override
        @SuppressWarnings("unchecked")
        public void process(List<ForwarderConfig> configList) throws MonitorException {

            for (ForwarderConfig config : configList) {
                String id = config.getId();
                logger.debug("changed:topic:{}, state:{}", id, config.getState());
                LogParser parser = parsers.remove(id);
                try {
                    if (config.getState() == Config.State.DELETE) {
                        if (parser != null)//CHANGED
                            parser.stop();
                        continue;
                    } else if (config.getState() == Config.State.UPDATE) {
                        if (parser != null)//changed
                            parser.stop();
                    }

                    String className = CLASS_MAPPING
                            .getProperty(config.get("parser"), RegexLogParser.class.getName());
                    Class<LogParser> parserClass = (Class<LogParser>) Class
                            .forName(className);
                    parser = parserClass.newInstance();

                    config.put("forwarder", forwarder);
                    parser.setConf(config);
                    threadPool.submit(parser);
                    parsers.put(id, parser);
                } catch (Exception e) {
                    logger.error("forward error:{}", e);
                }
            }
        }
    }
    // conf
    // -kafka reader
    // -parser
    // -es writer
    // -config();
    // TODO clearup();

}
