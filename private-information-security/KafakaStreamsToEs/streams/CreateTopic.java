package com.kafka.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

public class CreateTopic {
	
	//create topic
	public void createtopic() throws InterruptedException, ExecutionException {
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "172.17.104.15:9092");
		
		boolean topicexsit = false;
		
		try {
			
			//Adminclinet 생성하여 kafka와 연결
			AdminClient client = AdminClient.create(props);
			
			//kafka 서버에 존재하는 topiclist 생성
			ListTopicsOptions options = new ListTopicsOptions();
			options.listInternal(true); // includes internal topics such as __consumer_offsets
			ListTopicsResult topics = client.listTopics(options);
			Set<String> currentTopics = topics.names().get();
			
			//Topic 존재 여부 확인
			for (String currentTopic : currentTopics) {

				if (currentTopic.equals("streams-plaintext-input")) {
					topicexsit = true;
				}

			}
			
			//Topic 생성
			if (topicexsit) {
				final short replicationFactor = 1;
				final List<NewTopic> newTopics = Arrays
						.asList(new NewTopic("streams-plaintext-input", 1, replicationFactor));
				client.createTopics(newTopics).all().get();
			}
			// do your filter logic here......
		} catch (Exception e) {

		}
	}
}
