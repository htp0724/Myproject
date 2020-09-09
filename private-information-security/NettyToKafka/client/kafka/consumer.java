package com.hyun.client.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.net.*;
import java.io.*;

public class Consumer {
	private KafkaConsumer<Integer, String> consumer;
	private String topic = "testtopic1";
	private String groupId = "DemoConsumer";
	private int numMessageToConsume = 10000;
	private int messageRemaining = numMessageToConsume;
	private CountDownLatch latch = new CountDownLatch(2);
	private boolean readCommitted = false;

	public void set() throws UnknownHostException, IOException {
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.IntegerDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");

		if (readCommitted) {

			props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

		}

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		consumer = new KafkaConsumer<>(props);

	}

	KafkaConsumer<Integer, String> get() {
		return consumer;
	}

	public void doWork() throws UnknownHostException, IOException {
		/* consumer로 가져온 데이터를 전송하기 위한 코드
		   @SuppressWarnings("resource")
		   Socket socket = new Socket("172.17.2.55", 10003);
		   OutputStream output = socket.getOutputStream();
		*/
		
		//해당 topic을 읽기 위해 구독을 한다. 
		consumer.subscribe(Collections.singletonList(this.topic));
		
		//구독한 topic을 kafka consumer에서 데이터를 가져와서 저장
		ConsumerRecords<Integer, String> records = consumer.poll(Duration.ofSeconds(1));
		
		//가져온 데이터를 하나씩 print해주는 코드, key, value값과 offset값 까지 print 한다.
		for (ConsumerRecord<Integer, String> record : records) {
			System.out.println(groupId + " received message : from partition " + record.partition() + ", ("
					+ record.key() + ", " + record.value() + ") at offset " + record.offset());
			
			
			//consumer로 가져온 데이터를 전송하기 위한 코드
			// byte[] data = record.value().getBytes();
			// output.write(data);
			// socket.close();
		}
		
		
		messageRemaining -= records.count();
		
		if (messageRemaining <= 0) {
			System.out.println(groupId + " finished reading " + numMessageToConsume + " messages");
			latch.countDown();
		}
	}

}
