package com.kafka.streams.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {
	
	private String message;
	private String date;
	
	private Data data;
	
}
