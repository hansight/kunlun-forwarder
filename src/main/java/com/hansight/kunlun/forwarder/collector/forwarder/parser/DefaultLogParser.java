package com.hansight.kunlun.forwarder.collector.forwarder.parser;


import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.base.LogParser;
import com.hansight.kunlun.forwarder.collector.common.dao.ElasticSearchCacheDao;
import com.hansight.kunlun.forwarder.collector.common.exception.LogParserException;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.common.serde.AvroSerde;
import com.hansight.kunlun.forwarder.collector.common.utils.ForwarderConstants;
import com.hansight.kunlun.forwarder.collector.coordinator.config.ForwarderConfig;
import com.hansight.kunlun.forwarder.collector.coordinator.metric.MetricService;
import com.hansight.kunlun.forwarder.collector.coordinator.metric.ProcessorType;
import com.hansight.kunlun.forwarder.collector.forwarder.Forwarder;
import com.hansight.kunlun.forwarder.collector.mq.kafka.KafkaConsumer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui DateTime:2014/7/29 14:36.
 */
public abstract class DefaultLogParser implements LogParser<Runnable, Runnable> {
    protected final static Logger logger = LoggerFactory.getLogger(DefaultLogParser.class);
    protected final Integer cacheSize = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.LOG_PARSER_CACHE_SIZE, ForwarderConstants.LOG_PARSER_CACHE_SIZE_DEFAULT));
    protected final Integer cacheThreads = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.LOG_PARSER_CACHE_THREADS, ForwarderConstants.LOG_PARSER_CACHE_THREADS_DEFAULT));
    protected final Integer threadWaiting = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.LOG_PARSER_CACHE_THREAD_WAITING, ForwarderConstants.LOG_PARSER_CACHE_THREAD_WAITING_DEFAULT));
    private Boolean metric = Boolean.valueOf(Forwarder.GLOBAL.getProperty(ForwarderConstants.USE_METRIC, ForwarderConstants.USE_METRIC_DEFAULT));
    private String path = Forwarder.GLOBAL.getProperty(ForwarderConstants.METRIC_PATH, ForwarderConstants.METRIC_PATH_DEFAULT);


    protected ExecutorService threadPool;
    protected KafkaConsumer reader;
    public MetricService metricService;

    protected ForwarderConfig conf;
    protected Boolean running = true;

    @Override
    public void setConf(ForwarderConfig conf) {
        this.conf = conf;
        String topic = conf.getId();
        try {
            int partitions = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.KAFKA_METADATA_PARTITIONS, ForwarderConstants.KAFKA_METADATA_PARTITIONS_DEFAULT));
            reader = new KafkaConsumer(topic, partitions);
            threadPool = Executors.newFixedThreadPool(partitions + cacheThreads + 1);
            logger.debug("topic:{},type:{},category:{}", conf.get("id"), conf.get("type"), conf.get("category"));
            metricService = new MetricService(conf.getId(), conf.get("forwarder"), ProcessorType.FORWARDER);

        } catch (IOException e) {
            logger.error(" kafka connect error, please check your conf for this ,or see the kafka you conf is running !:{}", e);
        }
    }

    @Override
    public void parse() throws LogParserException {

        List<KafkaStream<byte[], byte[]>> streams = reader.getStreams();
        int i = 0;
        for (KafkaStream<byte[], byte[]> stream : streams) {
            threadPool.execute(new Processor(stream, i++));
        }
    }

    @Override
    public void run() {
        try {
            parse();
        } catch (Exception e) {
            logger.error("error:{}", e);
        }
    }

    class Processor extends Thread {
        protected List<Lexer> lexers;
        Queue<Event> cache = null;
        Lexer<Event, Map<String, Object>> lexer;
        long cachingFromTime;
        long heartbeat;
        private final KafkaStream<byte[], byte[]> stream;
        protected final AvroSerde serde = new AvroSerde();
        private final int part;

        public Processor(KafkaStream<byte[], byte[]> stream, int part) {
            this.part = part;
            this.stream = stream;
            lexers = new LinkedList<>();
            newCache();
        }

        private void heartbeat() {
            heartbeat = System.currentTimeMillis();

        }

        @SuppressWarnings("unchecked")
        public void process(KafkaStream<byte[], byte[]> stream) {
            for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                try {
                    if (!running) {
                        break;
                    }
                    parse(serde.deserialize(msgAndMetadata.message()));
                } catch (IOException e) {
                    logger.error("kafka  read event error :{}", e);
                } catch (InterruptedException e) {
                    logger.error("kafka  InterruptedException error :{}", e);
                }
            }
        }

        private void newCache() {
            cache = new LinkedList<>();
           /* if (metric) {
                try {
                    cache = new MetricQueue<>(Forwarder.class, path);
                } catch (IOException e) {
                    logger.error("metric error:{}", e);
                }
            } else {

            }*/
        }

        @SuppressWarnings("unchecked")
        private Lexer<Event, Map<String, Object>> get() throws InterruptedException {
            Lexer<Event, Map<String, Object>> lx;
            while (true) {
                for (Lexer<Event, Map<String, Object>> temp : lexers) {
                    if (temp.isDone()) {
                        temp.start();
                        return temp;
                    }
                }
                if (lexers.size() < cacheThreads) {
                    lx = getLexer();
                    lx.setMetric(metricService);
                    lx.setWriter(new ElasticSearchCacheDao<>(cacheSize, conf.get("type") + "_" + conf.get("category")));
                    lx.start();
                    lx.set("lexer " + (lexers.size() + 1));
                    lexers.add(lx);
                    return lx;
                }
                logger.warn("parse is too slow, waiting for {} ms.", threadWaiting);
                TimeUnit.MILLISECONDS.sleep(threadWaiting);
            }
        }

        private void flush() throws InterruptedException {
            lexer = get();
            lexer.setValueToParse(cache);
            threadPool.submit(lexer);
            newCache();
        }

        private void parse(Event event) throws InterruptedException {
            cache.add(event);
            heartbeat();
            if (cache.size() >= cacheSize) {
                logger.debug(" cache {} events take {} ms", cache.size(), System.currentTimeMillis() - cachingFromTime);
                flush();
                if (logger.isDebugEnabled()) {
                    cachingFromTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void run() {
            threadPool.execute(new Runnable() {
                /**
                 * an thread checking heartbeat when too long time no ,then flush data to parse 
                 */
                @Override
                public void run() {
                    while (running) {
                        try {
                            if ((System.currentTimeMillis() - heartbeat) > 100 * threadWaiting) {
                                if (cache.size() > 0) {
                                    logger.info("Long time no data entry, save the buffer(size:{}) to the database ", cache.size());
                                    flush();
                                }
                            }
                            TimeUnit.MILLISECONDS.sleep(100 * threadWaiting);
                        } catch (InterruptedException e) {
                            logger.error(" InterruptedException error :{}", e);
                        }
                    }
                }
            });
            process(this.stream);
        }
    }

    public abstract Lexer<Event, Map<String, Object>> getLexer();

    @Override
    public void stop() throws InterruptedException {
        this.running = false;
        while (!threadPool.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
            threadPool.shutdown();
        }
        logger.info("topic:{},stopped by user", conf.getId());
    }
}
