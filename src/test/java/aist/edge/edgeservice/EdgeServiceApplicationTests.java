package aist.edge.edgeservice;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.http.*;
import org.springframework.util.*;
import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EdgeServiceApplicationTests{

	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.
			builder().pullOnStartup(true)
			.file("src/test/resources/docker-compose.yml")
			//Trip Command Service
				.waitingForService("trip-management-cmd", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("trip-management-cmd", HealthChecks.toRespondOverHttp(8080,
						(port) -> port.inFormat("http://localhost:8092")))
			//Trip Query Service
				.waitingForService("trip-management-query", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("trip-management-query", HealthChecks.toRespondOverHttp(8080,
						(port) -> port.inFormat("http://localhost:8093")))
			//Discovery Service
				.waitingForService("discovery-service", HealthChecks.toHaveAllPortsOpen())
				.waitingForService("discovery-service", HealthChecks.toRespondOverHttp(8761,
						(port) -> port.inFormat("http://localhost:8761")))
			.build();

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void trip_POST_Request_Test() {
		//given:
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		Map<String, String> map = new HashMap<>();
		map.put("Content-Type", "application/json");
		headers.setAll(map);
		String body = "{ \"originAddress\": \"Somewhere of the origin\", \"destinationAddress\": \"Somewhere destination\"," +
				 "\"userId\": \"123e4567-e89b-12d3-a456-426655440000\" }";
		HttpEntity<String> request = new HttpEntity<>(body, headers);

		//when:
		ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8092/api/trip", request, String.class);

		//then:
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
	}

	@Test
	public void trip_GET_Request_Test() {
		//given:

		//when:
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8093/api/trip", String.class);

		//then:
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
	}
}
