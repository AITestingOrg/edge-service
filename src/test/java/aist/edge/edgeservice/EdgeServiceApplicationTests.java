package aist.edge.edgeservice;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.*;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class EdgeServiceApplicationTests{
	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.
			builder()
				.file("src/test/resources/docker-compose.yml")
				.waitingForService("microservice--trip-management", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("microservice--trip-management", HealthChecks.toRespondOverHttp(8181, (port) -> port.inFormat("http://localhost:8181/")))
				.waitingForService("microservice--payment-services", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("microservice--payment-services", HealthChecks.toRespondOverHttp(8082, (port) -> port.inFormat("http://localhost:8082/")))
				.build();

	@Autowired
	private TestRestTemplate testRestTemplate;

//	@Ignore
//	public void testTripServiceUnauthorizedAccess() throws Exception {
//		// given:
//		MockMvcRequestSpecification request = given().header("Content-Type", "application/json");
//
//		// when:
//		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:" + this.port + "/trips/", String.class);
//
//		// then:
//		assertThat(response.getStatusCodeValue()).isEqualTo(401);
//
//		// and:
//		DocumentContext parsedJson = JsonPath.parse(response.getBody());
//		Gson gson = new Gson();
//		AuthenticationError errorObject = gson.fromJson(response.getBody().toString(), AuthenticationError.class);
//
//		assertThat(errorObject.error).isEqualTo("unauthorized");
//		assertThat(errorObject.error_description).isEqualTo("Full authentication is required to access this resource");
//	}

	@Test
	public void tripSwagger() throws Exception {
		// given:
		MockMvcRequestSpecification request = given().header("Content-Type", "application/json");

		// when:
		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:8181/trips/", String.class);

		// then:
		assertThat(response.getStatusCodeValue()).isEqualTo(200);

		// and:
		assertThat(response.getBody()).contains("http://localhost:8181/trips/");
	}

	@Test
	public void paymentSwagger() throws Exception {
		// given:
		MockMvcRequestSpecification request = given().header("Content-Type", "application/json");

		// when:
		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:8082/payments/", String.class);

		// then:
		assertThat(response.getStatusCodeValue()).isEqualTo(200);

		// and:
		assertThat(response.getBody()).contains("http://localhost:8082/payments/");
	}

}
