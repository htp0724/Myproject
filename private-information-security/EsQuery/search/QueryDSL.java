package com.elastic.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.ParseException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.elastic.search.model.QueryDTO;
import com.elastic.search.model.RegexRecord;

public class QueryDSL {
	private String indexname = "testindex";
	private QueryDTO querydto = new QueryDTO();
	private int size = 100; 
	
	// prefix test
	public void prefix(String word, RestHighLevelClient client) throws ParseException, IOException {
		querydto.setWord(word);
		
		SearchRequest searchRequest = new SearchRequest(indexname);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(querydto.prefixQuery());
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
		
		FromJson.getJson(response);
	}

	// regexp query test
	public void regex(RestHighLevelClient client) throws ParseException, IOException {
		SearchRequest searchRequest = new SearchRequest(indexname);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(querydto.regexQuery());
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

		FromJson.getJson(response);
	}

	// wildcard query test
	public void wildcard(RestHighLevelClient client, String word) throws ParseException, IOException {
		querydto.setWord(word);

		SearchRequest searchRequest = new SearchRequest(indexname);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(querydto.wildcardQuery());
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

		FromJson.getJson(response);
	}

	// Like Inner Join
	public void andQueryResult(RestHighLevelClient client, String word1, String word2) throws IOException {
		querydto.setWord1(word1);
		querydto.setWord2(word2);
		
		SearchRequest searchRequest = new SearchRequest(indexname);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(querydto.andQuery());
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

		FromJson.getJson(response);
	}

	// Like Outer Join
	public void orQueryResult(RestHighLevelClient client, String word1, String word2) throws IOException {
		querydto.setWord1(word1);
		querydto.setWord2(word2);
		
		SearchRequest searchRequest = new SearchRequest(indexname);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(querydto.orQuery());
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

		FromJson.getJson(response);
	}	
	
	public ArrayList<RegexRecord> parsingofdata(String message, String date, ArrayList<RegexRecord> records) {
		RegexRecord record = new RegexRecord();
		// message field
		String regex = "([a-z]+),([0-9]{8}),([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}),([a-z]{1,5}),([a-z]{1,5}),([0-9]{10,11}),([a-z]+)";

		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(message);

		// find를 해줘야 group을 가져올 수 있음
		match.find();
		record.setMessage(message);
		record.setName(match.group(1));
		record.setIdate(match.group(2));
		record.setIp(match.group(3));
		record.setLast(match.group(4));
		record.setSex(match.group(5));
		record.setPhone(match.group(6));
		record.setJob(match.group(7));

		// data field
		regex = ".*";

		pat = Pattern.compile(regex);
		match = pat.matcher(date);

		match.find();
		record.setDate(match.group());

		records.add(record);

		return records;
	}
}
