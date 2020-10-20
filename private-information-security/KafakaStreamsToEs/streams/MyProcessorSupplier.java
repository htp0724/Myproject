import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import com.google.gson.Gson;
import com.kafka.streams.Authorization.Authorization;
import com.kafka.streams.model.Data;

class MyProcessorSupplier implements ProcessorSupplier<String, String> {
	
	@Override
	public Processor<String, String> get() {
		return new Processor<String, String>() {
			private Logger logger = LogManager.getLogger(MyProcessorSupplier.class);
			private ProcessorContext context;
			private KeyValueStore<String, Integer> kvStore;
			private Data data = new Data();
			private Gson gson = new Gson();
			
			private String indexname = "test_index";
			private String id = "user";
			private String password = "password";
			
			IndexRequest request = new IndexRequest(indexname);
			
			ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
				
				@Override
				public void onResponse(IndexResponse indexResponse) {
					logger.info(indexResponse.toString());
				}
				@Override
				public void onFailure(Exception e) {
					logger.info(e.toString());
				}
			};
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
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
				
				// 클라이언트 연결 및 index 생성
				RestHighLevelClient restClient = CreateIndex.createindex();
				
				// ID, password 방식 로그인
				// restClient = Authorization.highClientLogin(id, password);
				
				// 데이터 insert 쿼리생성
				/*
					Request request = new Request("POST", "/"+ indexname +"/_doc");
					request.setEntity(new NStringEntity(
					gson.toJson(data),
					ContentType.APPLICATION_JSON));
					Response response;
				*/
				
				request.source(gson.toJson(data), XContentType.JSON);
				
				// insert 요청
				restClient.indexAsync(request, RequestOptions.DEFAULT, listener);
				
				// 반드시 클라이언트를 닫아줘야함
				// restClient.close();
			}

			@Override
			public void close() {
			}
		};
	}
}
