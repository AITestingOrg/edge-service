package aist.edge.edgeservice;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class)
public class EdgeServiceApplicationTests{

	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.
			builder().pullOnStartup(true)
			.file("/Users/eriny/microservices/microservices--backing-service--edge-service/src/test/resources/docker-compose.yml")
			//User Service
//				.waitingForService("microservice--user-service", HealthChecks.toHaveAllPortsOpen())
//				.waitingForService("microservice--user-service", HealthChecks.toRespondOverHttp(8091,
//						(port) -> port.inFormat("http://localhost:8091/")))
			//Trip Command Service
				.waitingForService("mongo", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("mongo", HealthChecks.toRespondOverHttp(27017,
						(port) -> port.inFormat("http://localhost:27017")))
			//Trip Query Service
				.waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("rabbitmq", HealthChecks.toRespondOverHttp(15672,
						(port) -> port.inFormat("http://localhost:15672/")))
			.build();

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

//	@Before
//	public void setup() {
//		mockMvc = MockMvcBuilders
//			.webAppContextSetup(context)
//			.apply(springSecurity())
//			.build();
//	}

	@Test
	public void trip_Post_Request_Test() throws Exception {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.context)
				.build();
		this.mockMvc.perform(get("http://localhost:8092/api/trip").contentType(MediaType.APPLICATION_JSON)
						.content("{ \"originAddress\": \"Somewhere of the origin\", \"destinationAddress\": \"Somewhere destination\"," +
						" \"userId\": 123e4567-e89b-12d3-a456-426655440000 }"))
					.andExpect(status().isOk());
	}
//
//	private String getAccessToken(String username, String password) throws Exception {
//		MockHttpServletResponse response = mockMvc
//				.perform(post("/oauth/auth/token")
//						.header("Authorization", "Basic "
//								+ new String(Base64Utils.encode(("appclient:password")
//								.getBytes())))
//						.param("username", username)
//						.param("password", password)
//						.param("grant_type", "password"))
//				.andReturn().getResponse();
//	}

//	@Ignore
//	public void testUserAuthorizationService() throws Exception {
//
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//		params.add("grant_type", "password");
//		params.add("scope", "webclient");
//		params.add("username", "user1");
//		params.add("password", "$2a$04$NX3QTkBJB00upxKeaKqFBeoIVc9JHvwVnj1lItxNphRj34wNx5wlus");
//
//		// given:
//		MockMvcRequestSpecification request = given().params(params);
//
//		// when:
//		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:8091/oauth/auth/token", String.class);
//
//		// then:
//		assertThat(response.getStatusCodeValue()).isEqualTo(200);
//	}

//	@Test
//	public void tripSwagger() throws Exception {
//		// given:
//		MockMvcRequestSpecification request = given().header("Content-Type", "application/json");
//
//		// when:
//		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:8181/trips/", String.class);
//
//		// then:
//		assertThat(response.getStatusCodeValue()).isEqualTo(200);
//
//		// and:
//		assertThat(response.getBody()).contains("http://localhost:8181/trips/");
//	}

//	@Test
//	public void paymentSwagger() throws Exception {
//		// given:
//		MockMvcRequestSpecification request = given().header("Content-Type", "application/json");
//
//		// when:
//		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:8082/payments/", String.class);
//
//		// then:
//		assertThat(response.getStatusCodeValue()).isEqualTo(200);
//
//		// and:
//		assertThat(response.getBody()).contains("http://localhost:8082/payments/");
//	}

}
