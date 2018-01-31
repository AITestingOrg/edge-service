package aist.edge.edgeservice;

import com.google.gson.Gson;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

import static org.assertj.core.api.Assertions.assertThat;

import wiremock.com.jayway.jsonpath.DocumentContext;
import wiremock.com.jayway.jsonpath.JsonPath;

@Configuration
@EnableAutoConfiguration
@EnableEurekaServer
class EurekaServer {
}

class AuthenticationError {
	public String error;
	public String error_description;
}

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EdgeServiceApplicationTests{

	static ConfigurableApplicationContext eurekaServer;

	@BeforeClass
	public static void startEureka() {
		eurekaServer = SpringApplication.run(EurekaServer.class,
				"--server.port=8761",
				"--eureka.instance.leaseRenewalIntervalInSeconds=1");
	}

	@AfterClass
	public static void closeEureka() {
		eurekaServer.close();
	}

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void contextLoads() throws Exception {
	}

	@Test
	public void testTripServiceUnauthorizedAccess() throws Exception {
		// given:
		MockMvcRequestSpecification request = given().header("Content-Type", "application/json");

		// when:
		ResponseEntity<String> response = this.testRestTemplate.getForEntity("http://localhost:" + this.port + "/trips/", String.class);

		// then:
		assertThat(response.getStatusCodeValue()).isEqualTo(401);

		// and:
		DocumentContext parsedJson = JsonPath.parse(response.getBody());
		Gson gson = new Gson();
		AuthenticationError errorObject = gson.fromJson(response.getBody().toString(), AuthenticationError.class);

		assertThat(errorObject.error).isEqualTo("unauthorized");
		assertThat(errorObject.error_description).isEqualTo("Full authentication is required to access this resource");
	}
}
