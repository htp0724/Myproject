package com.elastic.search.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Data
public class RegexRecord {
	private String message;
	private String date;
	
	//regex
	private String name;
	private String idate;
	private String ip;
	private String last;
	private String sex;
	private String phone;
	private String job;
	
	@Builder
    public RegexRecord(String message, String date, String name, String idate, String ip, String last, String sex, String phone, String job) {
        this.message = message;
        this.date = date;
        this.name = name;
        this.idate = idate;
        this.ip = ip;
        this.last = last;
        this.sex = sex;
        this.phone = phone;
        this.job = job;
    }
}
