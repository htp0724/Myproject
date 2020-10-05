package com.java.test.model;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class Index {

	public static XContentBuilder indexmapping() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		
		builder.startObject();
		{
		    builder.startObject("properties");
		    {
		        builder.startObject("message");
		        {
		            builder.field("type", "keyword");
		        }
		        builder.endObject();
		        
		        builder.startObject("date");
		        {
		            builder.field("type", "date");
		            builder.field("format","yyyy-MM-dd HH:mm:ss");
		            builder.field("index", true);
		        }
		        builder.endObject();
		        
		        builder.startObject("performance");
		        {
		            builder.field("type", "keyword");
		        }
		        builder.endObject();
		    }
		    builder.endObject();
		}
		builder.endObject();

		return builder;
	}

}
