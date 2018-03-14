package aist.edge.edgeservice;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EdgeServiceIntegrationTests {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("microservice--user-service", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("microservice--user-service", HealthChecks.toRespondOverHttp(8091,
                (port) -> port.inFormat("http://localhost:8091")))
            .waitingForService("trip-management-cmd", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("trip-management-cmd", HealthChecks.toRespondOverHttp(8080,
                (port) -> port.inFormat("http://localhost:8092")))
            .waitingForService("trip-management-query", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("trip-management-query", HealthChecks.toRespondOverHttp(8080,
                (port) -> port.inFormat("http://localhost:8093")))
            .waitingForService("discovery-service", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("discovery-service", HealthChecks.toRespondOverHttp(8761,
                (port) -> port.inFormat("http://localhost:8761")))
            .build();

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private String token;

    @Before
    public void setUp() throws JSONException {
        String plainCreds = "eagleeye:thisissecret";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("username", "eagleeye");
        parameters.add("secret", "thisissecret");
        String body = "grant_type=password&scope=webclient&username=user1&password=password";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        //when:
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8091/auth/oauth/token", request, String.class, parameters);

        //then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        JSONObject json = new JSONObject(response.getBody());
        token = json.getString("access_token");
    }

    @Test
    public void tripCommandPOSTRequestSuccess() {
        //given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");

        String body = "{ \"originAddress\": \"Somewhere of the origin\", \"destinationAddress\": "
                + "\"Somewhere destination\", \"userId\": \"123e4567-e89b-12d3-a456-426655440000\" }";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        //when:
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8092/api/trip", request, String.class);

        //then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void tripQueryGETRequestSuccess() {
        //given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        //when:
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8093/api/trips", String.class);

        //then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}
