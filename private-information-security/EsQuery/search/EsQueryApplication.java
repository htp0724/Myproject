package com.elastic.search;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsQueryApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(EsQueryApplication.class);
	
	public static void main(String[] args) throws IOException {
		 
		SpringApplication.run(EsQueryApplication.class, args);
		int ch = 1;
		String word = "";
		String word2 = "";
		QueryDSL querydsl = new QueryDSL();
	    
		
		while (true) {
			
			/* 클러스터링 low level restclient 쿼리
			   RestClient restClient = RestClient.builder( 
			   new HttpHost("localhost", 9200, "http"),
			   new HttpHost("localhost", 9200, "http"),
			   new HttpHost("localhost", 9200, "http"), 
			   new HttpHost("localhost", 9201, "http")).build();
			 */
			
			RestHighLevelClient restClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost("172.17.104.17", 9200, "http")));
			
			logger.info("클라이언트 연결 완료.");
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(System.in);
			
			logger.info("prefix(맨처음부분만 like) 검색은 1, regexp는 2, wildcard는 3, And는 4, Or은 5");

			ch = sc.nextInt();
			if (ch == 1) {
				logger.info("검색할 word를 입력하세요.");
				word = sc.next();
				querydsl.prefix(word, restClient);
			}

			// 키워드 search
			else if (ch == 2) {
				querydsl.regex(restClient);
			}

			else if (ch == 3) {
				logger.info("검색할 word를 입력하세요.");
				word = sc.next();
				querydsl.wildcard(restClient, word);
			}
			
			else if (ch == 4) {
				logger.info("검색할 word1을 입력하세요.");
				word = sc.next();
				logger.info("검색할 word2를 입력하세요.");
				word2 = sc.next();
				querydsl.andQueryResult(restClient, word, word2);
			}
			
			else {
				logger.info("검색할 word를 입력하세요.");
				word = sc.next();
				logger.info("검색할 word2를 입력하세요.");
				word2 = sc.next();
				querydsl.orQueryResult(restClient, word, word2);
			}
			
			restClient.close();

		}
	}
}
