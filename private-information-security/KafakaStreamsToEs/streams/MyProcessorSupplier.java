package com.kafka.streams;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.google.gson.Gson;
import com.kafka.streams.model.Data;

class MyProcessorSupplier implements ProcessorSupplier<String, String> {
	
	@Override
	public Processor<String, String> get() {
		return new Processor<String, String>() {
			private Logger logger = LogManager.getLogger(MyProcessorSupplier.class);
			private String indexname = "testindex";
			private ProcessorContext context;
			private KeyValueStore<String, Integer> kvStore;
			private Data data = new Data();
			private Gson gson = new Gson();
			// 프로세서 생성 시 초기화
			@Override
			@SuppressWarnings("unchecked")
			public void init(final ProcessorContext context) {
				this.context = context;

				// 타임스탬프 설정
				this.context.schedule(Duration.ofSeconds(1), PunctuationType.STREAM_TIME, timestamp -> {
					try (final KeyValueIterator<String, Integer> iter = kvStore.all()) {
						System.out.println("----------- " + timestamp + " ----------- ");

						while (iter.hasNext()) {
							final KeyValue<String, Integer> entry = iter.next();

							System.out.println("[" + entry.key + ", " + entry.value + "]");

							context.forward(entry.key, entry.value.toString());
						}
					}
				});

				// statestore 이름 명명
				this.kvStore = (KeyValueStore<String, Integer>) context.getStateStore("Counts");
			}

			// elasticsearch 전송
			@Override
			public void process(final String dummy, final String line) {
		
				//날짜 생성
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
				String nowString = now.format(dateTimeFormatter);
				data.setMessage(line);
				data.setDate(nowString);
				
				// 텍스트 스플릿
				// final String[] words = line.toLowerCase(Locale.getDefault()).split(" ");
				
				/* 클러스터링 low level restclient 쿼리
				   RestClient restClient = RestClient.builder( 
				   new HttpHost("localhost", 9300, "http"),
				   new HttpHost("localhost", 9300, "http"),
				   new HttpHost("localhost", 9300, "http"), 
				   new HttpHost("localhost", 9300, "http")).build();
				 */
				
				// 클라이언트 연결
				RestClient restClient = RestClient.builder(new HttpHost("172.17.104.17", 9200, "http")).build();
				
				// 데이터 insert 쿼리생성
				Request request = new Request("POST", "/"+ indexname +"/_doc");
				request.setEntity(new NStringEntity(
						gson.toJson(data),
						ContentType.APPLICATION_JSON));
				Response response;

				try {
					
					// insert 요청
					response = restClient.performRequest(request);
					logger.info(response);
					
					// 반드시 클라이언트를 닫아줘야함
					restClient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void close() {
			}
		};
	}
}
