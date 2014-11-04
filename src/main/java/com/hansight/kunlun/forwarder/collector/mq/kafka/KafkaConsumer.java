package com.hansight.kunlun.forwarder.collector.mq.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.common.serde.AvroSerde;
import com.hansight.kunlun.forwarder.collector.forwarder.Forwarder;
import com.hansight.kunlun.forwarder.collector.utils.Common;

public class KafkaConsumer {
    private static final String GROUP_ID = "group.id";
    private ConsumerConnector consumerConnector;
    private Map<String, Integer> map = new HashMap<>();
    private String topic;

    public KafkaConsumer(String topic, int partitionsNum) throws IOException {
        this.topic = topic;
        // specify some consumer properties

        // Create the connection to the cluster

        // create 4 partitions of the stream for topic “test-topic”, to allow 4
        // threads to consume

        map.put(topic, partitionsNum);
        Properties props = new Properties();
        props.put(Common.ZOOKEEPER_CONNECT, Forwarder.GLOBAL.getProperty(Common.ZOOKEEPER_CONNECT, "local:2181"));
        props.put(Common.ZOOKEEPER_CONNECTION_TIMEOUT, Forwarder.GLOBAL.getProperty(Common.ZOOKEEPER_CONNECTION_TIMEOUT, "1000000"));
        props.put(GROUP_ID, Forwarder.GLOBAL.getProperty(GROUP_ID, ""));
        props.put(Common.AUTO_COMMIT_ENABLE, Forwarder.GLOBAL.getProperty(Common.AUTO_COMMIT_ENABLE, "true"));
        props.put(Common.AUTO_COMMIT_INTERVAL_MS, Forwarder.GLOBAL.getProperty(Common.AUTO_COMMIT_INTERVAL_MS, "6000"));
        props.put(Common.AUTO_OFFSET_RESET, Forwarder.GLOBAL.getProperty(Common.AUTO_OFFSET_RESET, "smallest"));
        props.put(Common.ZOOKEEPER_SESSION_TIMEOUT_MS, Forwarder.GLOBAL.getProperty(Common.ZOOKEEPER_SESSION_TIMEOUT_MS, "30000"));
        ConsumerConfig consumerConfig = new ConsumerConfig(props);
        //  consumer=new SimpleConsumer(ForwarderConfig.GLOBAL.getProperty();
        consumerConnector = Consumer
                .createJavaConsumerConnector(consumerConfig);
        // create list of 4 threads to consume from each of the partitions
        //    setup();
    }

    public List<KafkaStream<byte[], byte[]>> getStreams() {

        Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams = consumerConnector
                .createMessageStreams(map);
        //   consumerConnector.commitOffsets();
        return topicMessageStreams
                .get(topic);
    }

    public void executor() {
        ExecutorService executor = Executors.newCachedThreadPool();

        List<KafkaStream<byte[], byte[]>> streams = getStreams();
        System.out.println("streams = " + streams.size());
        // consume the messages in the threads
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            System.out.println("stream = " + stream.size());
            executor.submit(new MyProcessor(stream));
        }
    }

    public static interface Processor extends Runnable {
        void process();
    }

    public static class MyProcessor implements Processor {
        private final KafkaStream<byte[], byte[]> stream;

        public MyProcessor(KafkaStream<byte[], byte[]> stream) {
            this.stream = stream;
        }

        final AvroSerde serde = new AvroSerde();

        @Override
        public void process() {
            for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                Event event;
                try {
                    event = serde.deserialize(msgAndMetadata.message());

                    // process message (msgAndMetadata.message())
                    System.out.println("topic: " + msgAndMetadata.topic());
                    try {
                        System.out.println("message content: "
                                + new String(event.getBody().array(), "utf-8"));
                        //   consumerConnector.commitOffsets();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
              process();
        }
    }

    public static void main(String[] args) throws IOException {
        new KafkaConsumer("100000000000000", 1).executor();
    }
}
