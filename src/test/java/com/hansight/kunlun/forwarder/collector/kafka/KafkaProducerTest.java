package com.hansight.kunlun.forwarder.collector.kafka;

import java.io.IOException;
import com.hansight.kunlun.forwarder.collector.common.exception.CollectorException;

public class KafkaProducerTest {
	private static String topic = "test1";

	public static void main(String[] args) throws CollectorException,
			IOException, InterruptedException {
//		AvroSerde serde = new AvroSerde();
//		Producer<String, byte[]> producer = new Producer<>(new ProducerConfig());
//		while (true) {
//			Event event = new Event();
//			event.setBody(ByteBuffer.wrap("asdjas".getBytes()));
//			KeyedMessage<String, byte[]> km = new KeyedMessage<>(topic,
//					serde.serialize(event));
//			producer.send(km);
//			TimeUnit.MILLISECONDS.sleep(100);
//		}
	}
}
