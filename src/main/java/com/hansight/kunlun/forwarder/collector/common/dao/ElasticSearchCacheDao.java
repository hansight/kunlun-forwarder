package com.hansight.kunlun.forwarder.collector.common.dao;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.forwarder.collector.common.base.LogWriter;
import com.hansight.kunlun.forwarder.collector.common.exception.LogWriteException;
import com.hansight.kunlun.forwarder.collector.forwarder.parser.ESIndexMaker;
import com.hansight.kunlun.forwarder.collector.utils.Pair;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui_yan
 * DateTime:2014/7/29 16:43.
 */
public class ElasticSearchCacheDao<T extends Map<String, Object>> extends ESDao<T> implements LogWriter<T> {
    protected ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private ESIndexMaker maker = new ESIndexMaker();
    final String mark = "@timestamp";
    final String indexPre = "logs_";
    private int cacheSize = 1;
    private int caches = 0;
    private long times = 0;
    protected final static Logger logger = LoggerFactory.getLogger(ElasticSearchCacheDao.class);
    private BulkRequestBuilder builder;
    private List<Executor> executors = new LinkedList<>();

    public ElasticSearchCacheDao(int cacheSize, String type) {
        this.cacheSize = cacheSize;
        this.setType(type);
    }

    public Map<String, Object> toMap(T t) {
        return t;
    }

    public boolean save(T t) {
        if (builder == null) {
            builder = client.prepareBulk();
        }
        builder.add(client.prepareIndex(index, type)
                .setSource(toMap(t)));
        caches++;
        if (caches >= cacheSize) {
            flush();
        }
        return true;
    }

   private class Executor implements Runnable {
        private BulkRequestBuilder builder;
        private int caches = 0;
        private boolean done = false;

        public void setBuilder(BulkRequestBuilder builder) {
            this.builder = builder;
        }

        public void setCaches(int caches) {
            this.caches = caches;
        }

        @Override
        public void run() {
            done = false;
            long start = 0;
            if (logger.isInfoEnabled()) {
                start = System.currentTimeMillis();
            }
            if (builder == null) {
                logger.info("no data need to save to es");
                return;
            }
            try {
                BulkResponse response = builder.execute().actionGet();
                if (response.hasFailures()) {
                    logger.error("es save has error info :{}", response.buildFailureMessage());
                }

            } catch (Exception e) {
                logger.error("es save has error info :{}", e);
            }
            builder = null;
            logger.info("save  {} data take {} ms", caches, System.currentTimeMillis() - start);
            done = true;
        }

        public boolean isDone() {
            return done;
        }

        public void start() {
            this.done = false;
        }
    }

    private Executor get(BulkRequestBuilder builder, int caches) {
        Executor executor = null;
        label:
        while (true) {
            for (Executor temp : executors) {
                if (temp.isDone()) {
                    executor = temp;
                    break label;
                }
            }
            if (executors.size() < 5) {
                executor = new Executor();
                break;
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                    logger.info(" save to es too slow waiting:{} ms", 500);
                } catch (InterruptedException e) {
                    logger.error("es save has error info :{}", e);
                }
            }
        }

        executor.start();
        executor.setBuilder(builder);
        executor.setCaches(caches);
        return executor;
    }

    @Override
    public void flush() {
        get(builder, caches).run();
        //threadPool.execute(get(builder, caches));
        builder = null;
        caches = 0;
    }

    @Override
    public void close() {
        if (caches > 0)
            flush();
    }

    @Override
    public void write(T t) throws LogWriteException {
        if (t.get("error") != null) {
            t.put("index", "parse_error");
            t.put(mark, new Date());
        } else {
            Pair<String, Date> pair;
            try {
                pair = maker.indexSuffix(t);
            } catch (ParseException e) {
                throw new LogWriteException("index maker error", e);
            }
            t.put(mark, pair.second());
            String index = pair.first().split(" ")[0];
            t.put("index", indexPre + index);
        }
        this.setIndex(t.remove("index").toString());
        save(t);
    }
}