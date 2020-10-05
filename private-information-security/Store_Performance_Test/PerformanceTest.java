package com.java.test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.java.test.model.Data;

public class PerformanceTest {

	private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);
	
	public static void performanceTest(int performance_count) throws IOException {
		String indexname = "performance_index";
		Data data = new Data();
		
		RestHighLevelClient restClient = new RestHighLevelClient(
				RestClient.builder(new HttpHost("172.17.104.17", 9200, "http")));
		
		char[] datas = new char[2250];
		String testdata = new String(datas);
		testdata = "" + testdata;
		
		int count = 1;
		BulkRequest bulkrequest = new BulkRequest();
		
		logger.info("performance test start");
			
		ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
		    @Override
		    public void onResponse(BulkResponse bulkResponse) {
		    	
		        logger.info(bulkResponse.toString());
		    }

		    @Override
		    public void onFailure(Exception e) {
		    	logger.error(e.toString());
		    }
		};
		
		// HeadBufferedResponse Option 설정 (현재 적용되지 않음)
		RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
		builder.setHttpAsyncResponseConsumerFactory(
				new HttpAsyncResponseConsumerFactory
				.HeapBufferedResponseConsumerFactory(5*1024*1024));
		RequestOptions COMMON_OPTIONS = builder.build();
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter putdateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String nowString;
		String firstString = now.format(putdateTimeFormatter);
		
		long firstTime = System.currentTimeMillis();
		
		while (count < 1000) {
			
			now = LocalDateTime.now();
			nowString = now.format(putdateTimeFormatter);
			
			data.setMessage(testdata);
			data.setDate(nowString);
			
			bulkrequest.add(new IndexRequest(indexname)
					.source(datamapping(data.getMessage(), data.getDate())));
			
			if (count % 50 == 0) {
				restClient.bulkAsync(bulkrequest, RequestOptions.DEFAULT, listener);
			}
			
			if (count == 1000) {
				long lastTime = System.currentTimeMillis();
				bulkrequest.add(new IndexRequest(indexname).id(""+performance_count).source(XContentType.JSON, "performance",
						"100만 데이터 경과시간(초) : " + ((lastTime - firstTime) / 1000)));
				bulkrequest.add(new UpdateRequest(indexname, ""+performance_count)
						.doc(XContentType.JSON, "date", data.getDate()));
				restClient.bulkAsync(bulkrequest, RequestOptions.DEFAULT, listener);
			}
			count += 1;
		}

		logger.info("performance test end");
		String lastString = now.format(putdateTimeFormatter);
		logger.info("first input time = : " + firstString + "last input time : " + lastString);
	}
	
	public static XContentBuilder datamapping(String message, String date) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();

		builder.startObject();
		{
			builder.field("message", message);
			builder.field("date", date);
		}
		builder.endObject();

		return builder;
	}
}
