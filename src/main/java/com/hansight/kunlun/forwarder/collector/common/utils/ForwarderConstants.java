package com.hansight.kunlun.forwarder.collector.common.utils;

/**
 * Author:tao_zhang DateTime:2014/7/29 16:05.
 */
public interface ForwarderConstants {
    public static final String FORWARDER_CONF = "forwarder.properties";
    //public static final String FORWARDER_USER_CONF = "forwarder.conf";
    public static final String CLASS_MAPPING_CONF = "forwarder-class-mapping.properties";

    public static final String ASSET_INDEX_NAME = "kunlun";
    public static final String ASSET_TYPE_NAME = "conf";
    public static final String LOG_PARSER_POOL_SIZE = "log.parser.pool.size";
    public static final String LOG_PARSER_POOL_SIZE_DEFAULT = "10";
    public static final String LOG_PARSER_CACHE_THREADS = "log.parser.cache.threads";
    public static final String LOG_PARSER_CACHE_THREADS_DEFAULT= "5";
    public static final String LOG_PARSER_CACHE_THREAD_WAITING = "log.parser.cache.thread.waiting.ms";
    public static final String LOG_PARSER_CACHE_THREAD_WAITING_DEFAULT= "500";

    public static final String LOG_PARSER_CACHE_SIZE = "log.parser.cache.size";
    public static final String LOG_PARSER_CACHE_SIZE_DEFAULT = "1000";
    public static final String KAFKA_METADATA_PARTITIONS = "kafka.metadata.partitions";
    public static final String KAFKA_METADATA_PARTITIONS_DEFAULT = "1";

    public static final String METRIC_PATH = "metric.path";
    public static final String METRIC_PATH_DEFAULT = "./";

    public static final String USE_METRIC = "use.metric";
    public static final String USE_METRIC_DEFAULT = "true";

    public static final String FORWARDER_ID = "forwarder.id";
    public static final String FORWARDER_ID_DEFAULT = "test";


}
