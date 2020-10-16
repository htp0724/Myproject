import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.CreateTokenRequest;
import org.elasticsearch.client.security.CreateTokenResponse;
import org.elasticsearch.client.security.GetUsersRequest;
import org.elasticsearch.client.security.GetUsersResponse;
import org.elasticsearch.client.security.PutUserRequest;
import org.elasticsearch.client.security.PutUserResponse;
import org.elasticsearch.client.security.RefreshPolicy;
import org.elasticsearch.client.security.user.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthorizationApplication {
	protected static Logger logger = LogManager.getLogger(AuthorizationApplication.class);
	
	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(AuthorizationApplication.class, args);
		
		RestHighLevelClient restClient;
		
		// ID, password 방식 로그인
		restClient = login();
		
		// token 발급
		CreateTokenResponse createTokenResponse = get_token(restClient);
		logger.info(createTokenResponse.toString());
		
		String accessToken = createTokenResponse.getAccessToken();
		
		// token 로그인
		token_login(restClient, accessToken);
		
		
		
		// restClient.close();	
		
	}
	
	public static RestHighLevelClient login() {
		final CredentialsProvider credentialsProvider =
				new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "password"));
		
		RestHighLevelClient restClient = new RestHighLevelClient(
				RestClient.builder(new HttpHost("172.17.2.55", 9200, "http"))
				.setHttpClientConfigCallback(new HttpClientConfigCallback() {
					
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(
							HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder
								.setDefaultCredentialsProvider(credentialsProvider);
					}
				}));
		
		logger.info(restClient.toString());
		return restClient;
	}
	
	public static CreateTokenResponse get_token(RestHighLevelClient restClient) throws IOException, InterruptedException {
		final char[] password = new char[]{'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
		
		CreateTokenRequest createTokenRequest = CreateTokenRequest.passwordGrant("elastic", password);
		CreateTokenResponse createTokenResponse = restClient.security().createToken(createTokenRequest, RequestOptions.DEFAULT);
		
		return createTokenResponse;
	}
	
	public static void token_login(RestHighLevelClient restClient, String accessToken) throws IOException {
		Header[] defaultHeaders =
				new Header[] {new BasicHeader("Authorization",
						"Bearer " + accessToken)};
		
		restClient = new RestHighLevelClient(
				RestClient.builder(new HttpHost("172.17.2.55", 9200, "http"))
				.setDefaultHeaders(defaultHeaders));
		
		logger.info("Token 로그인 테스트");
		
		get_user(restClient);
	}
	
	// 유저 ID 생성 (역할 : superuser)
		public static void put_user(RestHighLevelClient restClient) throws IOException {
			char[] password = new char[]{'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
			
			User user = new User("elastic", Collections.singletonList("superuser"));
			PutUserRequest request = PutUserRequest.withPassword(user, password, true, RefreshPolicy.NONE);
			PutUserResponse response = restClient.security().putUser(request, RequestOptions.DEFAULT);
			
			boolean isCreated = response.isCreated();
			
			logger.info("user created : " + isCreated);
		}
		
		// 유저 목록 가져오기
		public static void get_user(RestHighLevelClient restClient) throws IOException {
			GetUsersRequest request = new GetUsersRequest();
			GetUsersResponse response = restClient.security().getUsers(request, RequestOptions.DEFAULT);

			List<User> users = new ArrayList<>(1);
			users.addAll(response.getUsers());
			
			for (User user : users) {		
				logger.info("username : " + user.getUsername());
			}
		}
}
