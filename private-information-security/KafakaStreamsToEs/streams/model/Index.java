package com.kafka.streams.model;

import com.google.gson.JsonObject;

public class Index {
	private JsonObject jobject;
	private JsonObject mobject;
	private JsonObject vobject;
	
	public String indexmapping() {
		jobject = new JsonObject();
		mobject = new JsonObject();
		vobject = new JsonObject();
		
		vobject.addProperty("type", "keyword");
		mobject.add("message", vobject);
		jobject.add("properties", mobject);
		
		/*
		"{\n" + 
		"  \"properties\": {\n" + 
		"    \"message\": {\n" + 
		"      \"type\": \"keyword\"\n" + 
		"    }\n" + 
		"  }\n" + 
		"}";
		*/
		
		return jobject.toString();
	}
	
}
