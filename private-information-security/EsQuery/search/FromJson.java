package com.elastic.search;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.elastic.search.model.QueryRecord;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FromJson {
	private static Logger logger = LogManager.getLogger(FromJson.class);
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static QueryRecord queryRecord = new QueryRecord();
	private static ArrayList<QueryRecord> queryRecords = new ArrayList<>();

	public static void getJson(SearchResponse response) throws IOException {
		// ArrayList<Record> records = new ArrayList<Record>();

		// json으로 필요한 object만 골라내기
		try {
			SearchHits searchHits = response.getHits();
			SearchHit[] hits = searchHits.getHits();

			for (SearchHit hit : hits) {
				String source = hit.getSourceAsString();
				queryRecord = objectMapper.readValue(source, QueryRecord.class);
				queryRecords.add(queryRecord);
				logger.info("message : " + queryRecord.getMessage() + " date : " + queryRecord.getDate());
			}

		} catch (Exception e) {
			logger.error("catch : " + e);
		}
	}
}
