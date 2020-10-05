package com.java.test;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;

import com.java.test.model.Index;

public class CreateIndex {
	
	private static final Logger logger = LogManager.getLogger(CreateIndex.class);
	private static String indexname = "performance_index";
	
	public static void createindex() {
		
		Index index = new Index();
		
		RestHighLevelClient restClient = new RestHighLevelClient(
				RestClient.builder(new HttpHost("172.17.104.17", 9200, "http"))
				.setRequestConfigCallback(
				        new RestClientBuilder.RequestConfigCallback() {
				            @Override
				            public RequestConfig.Builder customizeRequestConfig(
				                    RequestConfig.Builder requestConfigBuilder) {
				                return requestConfigBuilder
				                    .setConnectTimeout(5000)
				                    .setSocketTimeout(60000);
				            }
				        }));

		
		// index의 존재유무 확인
		GetIndexRequest grequest = new GetIndexRequest(indexname);
		boolean exists;
		
		try {
			exists = restClient.indices().exists(grequest, RequestOptions.DEFAULT);;
			
			// index 생성
			if (exists == false) {
				CreateIndexRequest request = new CreateIndexRequest(indexname);
				request.mapping(index.indexmapping());
				CreateIndexResponse createIndexResponse = restClient.indices().create(request, RequestOptions.DEFAULT);
				logger.info(createIndexResponse);
			}
			
			restClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
