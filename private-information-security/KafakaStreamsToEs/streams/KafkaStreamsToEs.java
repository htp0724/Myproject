package com.kafka.streams;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaStreamsToEs {


	public static void main(final String[] args) throws InterruptedException, ExecutionException {
		// 클러스터링 서버 설정
		// props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094,localhost:9095");
		
		final Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-word-processor");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		
		//Topic생성
		CreateTopic CreateTopic = new CreateTopic();
		CreateTopic.createtopic();
		
		//Index생성
		CreateIndex createindex = new CreateIndex();
		createindex.createindex();
		
		// setting offset reset to earliest so that we can re-run the demo code with the
		// same pre-loaded data
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// 토폴로지 생성
		final Topology builder = new Topology();

		// input받을 source 생성
		builder.addSource("Source", "streams-plaintext-input");

		// 데이터에 대한 처리를 담당하는 프로세서 생성
		builder.addProcessor("Process", new MyProcessorSupplier(), "Source");

		// storage engine을 관리하는 statestore생성
		builder.addStateStore(Stores.keyValueStoreBuilder(
				// inmemory방식으로 데이터 저장
				// process에서 받아들이기로 정의한 statestore의 이름 대입
				Stores.inMemoryKeyValueStore("Counts"), Serdes.String(), Serdes.Integer()), "Process");

		// consumer에게 전송할 sink생성
		builder.addSink("Sink", "streams-word-processor-output", "Process");

		// streams에 현재 topology와, 설정 대입
		final KafkaStreams streams = new KafkaStreams(builder, props);

		// countdownlatch 설정
		final CountDownLatch latch = new CountDownLatch(1);

		// attach shutdown handler to catch control-c
		Runtime.getRuntime().addShutdownHook(new Thread("streams-word-shutdown-hook") {
			@Override
			public void run() {
				// 쓰레드 동작 정지시 countdown 실행
				streams.close();
				latch.countDown();
			}
		});

		try {
			streams.start();

			// 쓰레드 동작이 0이 되는 순간 대기 상태 해체
			latch.await();
		} catch (final Throwable e) {
			System.exit(1);
		}
		System.exit(0);
	}
}
