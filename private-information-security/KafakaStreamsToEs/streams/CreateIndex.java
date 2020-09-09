package com.kafka.streams;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import com.kafka.streams.model.Index;

public class CreateIndex {
	
	private static final Logger logger = LogManager.getLogger(CreateIndex.class);
	
	public void createindex() {
		/*
		 * 클러스터링 High level restclient 쿼리 RestHighLevelClient client = new
		 * RestHighLevelClient( RestClient.builder( new HttpHost("localhost", 9300,
		 * "http"), new HttpHost("localhost", 9300, "http"), new HttpHost("localhost",
		 * 9300, "http"), new HttpHost("localhost", 9300, "http")));
		 */

		// index를 생성요청할 client 생성
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("172.17.104.17", 9200, "http")));
		
		Index index = new Index();
		String indexname = "testindex";
		
		// index의 존재유무 확인
		GetIndexRequest grequest = new GetIndexRequest(indexname);
		boolean exists;
		try {
			
			exists = client.indices().exists(grequest, RequestOptions.DEFAULT);;
			// index 생성
			if (exists == false) {
				CreateIndexRequest request = new CreateIndexRequest(indexname);
				request.mapping(index.indexmapping(), XContentType.JSON);
				CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
				logger.info(createIndexResponse);
			}
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
