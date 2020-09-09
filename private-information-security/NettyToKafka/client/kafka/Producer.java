package com.hyun.client.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class Producer {

	private String msgs = null;
	private KafkaProducer<Integer, String> producer;
	private String topic = "testtopic1";
	private Boolean isAsync = true;
	private CountDownLatch latch = new CountDownLatch(2);
	private String transactionalId = null;
	private int transactionTimeoutMs = -1;
	private boolean enableIdempotency = false;

	public void set() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "DemoProducer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		if (transactionTimeoutMs > 0) {
			props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, transactionTimeoutMs);
		}
		if (transactionalId != null) {
			props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
		}
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotency);

		producer = new KafkaProducer<>(props);
	}

	KafkaProducer<Integer, String> get() {
		return producer;
	}

	public void sendmsgs(String nettymsgs) {
		msgs = nettymsgs;
		String messageStr = msgs;
		long startTime = System.currentTimeMillis();
		topic = "streams-plaintext-input";

		if (isAsync) { // Send asynchronously
			producer.send(new ProducerRecord<>(topic, messageStr), new DemoCallBack(startTime, messageStr));
		} else { // Send synchronously
			try {
				producer.send(new ProducerRecord<>(topic, messageStr)).get();
				System.out.println("Sent message: (" + messageStr + ")");
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		latch.countDown();

	}

	public void getmsg(String nettymsg) {
		msgs = nettymsg;
	}
}

class DemoCallBack implements Callback {

	private final long startTime;
	private final String message;

	public DemoCallBack(long startTime, String message) {
		this.startTime = startTime;
		this.message = message;
	}

	/**
	 * A callback method the user can implement to provide asynchronous handling of
	 * request completion. This method will be called when the record sent to the
	 * server has been acknowledged. When exception is not null in the callback,
	 * metadata will contain the special -1 value for all fields except for
	 * topicPartition, which will be valid.
	 *
	 * @param metadata  The metadata for the record that was sent (i.e. the
	 *                  partition and offset). An empty metadata with -1 value for
	 *                  all fields except for topicPartition will be returned if an
	 *                  error occurred.
	 * @param exception The exception thrown during processing of this record. Null
	 *                  if no error occurred.
	 */
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		if (metadata != null) {
			System.out.println("message(" + message + ") sent to partition(" + metadata.partition() + "), " + "offset("
					+ metadata.offset() + ") in " + elapsedTime + " ms");
		} else {
			exception.printStackTrace();
		}
	}
}
