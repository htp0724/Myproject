package com.elastic.search.model;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import lombok.Data;

@Data
public class QueryDTO {
	private String indexname;
	private String word;
	private String word1;
	private String word2;
	private String field;
	
	private String fieldname = "message";
	
	private QueryBuilder query;
	private QueryBuilder query1;
	private QueryBuilder query2;
	private List<QueryBuilder> queryb;
	
	public void queryBuilderListinit() {
		queryb = new ArrayList<>();
	}
	
	// Query
	public QueryBuilder prefixQuery() {
		query = QueryBuilders.prefixQuery(fieldname , word);
		
		return query;
	}

	public QueryBuilder regexQuery() {
		query = QueryBuilders.regexpQuery(fieldname ,
				"\"([a-z]+),([0-9]{8}),([0-9]{1,3}\\\\.[0-9]{1,3}\\\\.[0-9]{1,3}\\\\.[0-9]{1,3}),"
				+ "([a-z]{1,5}),([a-z]{1,5}),([0-9]{10,11}),[a-z]+\"");
		
		return query;
	}

	public QueryBuilder wildcardQuery() {
		query = QueryBuilders.wildcardQuery("message", "*" + word + "*");
		
		return query;
	}

	public QueryBuilder andQuery() {
		queryBuilderListinit();
		
		query1 = QueryBuilders.wildcardQuery(fieldname , "*" + word1 + "*");
		query2 = QueryBuilders.wildcardQuery(fieldname , "*" + word2 + "*");
		
		queryb.add(query1);
		queryb.add(query2);
		BoolQueryBuilder filter = new BoolQueryBuilder();
		filter.must().addAll(queryb);
		
		query = filter;
				
		return query;
	}

	public QueryBuilder orQuery() {
		queryBuilderListinit();
		
		query1 = QueryBuilders.wildcardQuery(fieldname , "*" + word1 + "*");
		query2 = QueryBuilders.wildcardQuery(fieldname , "*" + word2 + "*");
		
		queryb.add(query1);
		queryb.add(query2);
		BoolQueryBuilder filter = new BoolQueryBuilder();
		filter.should().addAll(queryb);
		
		query = filter;
		
		return query;
	}
}
