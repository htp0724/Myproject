import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import com.kafka.streams.Authorization.Authorization;
import com.kafka.streams.model.Index;

public class CreateIndex {
	
	private static final Logger logger = LogManager.getLogger(CreateIndex.class);
	private static String id = "user";
	private static String password = "password";
	private static String indexname = "performance_test";
	
	public static RestHighLevelClient createindex() {
		
		Index index = new Index();
		
		RestHighLevelClient restClient = Authorization.highClientLogin(id, password);
		
		// index의 존재유무 확인
		GetIndexRequest grequest = new GetIndexRequest(indexname);
		boolean exists;
		
		try {
			exists = restClient.indices().exists(grequest, RequestOptions.DEFAULT);;
			
			// index 생성
			if (exists == false) {
				CreateIndexRequest request = new CreateIndexRequest(indexname);
				request.mapping(index.indexmapping(), XContentType.JSON);
				CreateIndexResponse createIndexResponse = restClient.indices().create(request, RequestOptions.DEFAULT);
				logger.info(createIndexResponse);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return restClient;
	}
}
