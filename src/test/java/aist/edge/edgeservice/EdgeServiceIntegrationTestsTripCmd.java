package aist.edge.edgeservice;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EdgeServiceIntegrationTestsTripCmd {
    protected static final Logger LOG = LoggerFactory.getLogger(EdgeServiceIntegrationTestsTripCmd.class);

    private static String discoveryServiceURL;
    private static String mongoURL;
    private static String userServiceURL;
    private static String tripCommandURL;

    // Wait for all services to have ports open
    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
            .file("src/test/resources/docker-compose-trip-cmd.yml")
            .waitingForService("discoveryservice", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("mysqlserver", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("mongo", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("userservice", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("tripmanagementcmd", HealthChecks.toHaveAllPortsOpen())
            .build();

    // Get IP addresses and ports to run tests on
    @BeforeClass
    public static void initialize() {

        LOG.info("Initializing ports from Docker");

        DockerPort discoveryService = docker.containers().container("discoveryservice").port(8761);
        discoveryServiceURL = String.format("http://%s:%s", discoveryService.getIp(),
                discoveryService.getExternalPort());
        while (!docker.containers().container("discoveryservice")
                .portIsListeningOnHttp(8761, (port) -> port.inFormat(discoveryServiceURL)).succeeded()) {
            LOG.info("Waiting for discovery service to respond over HTTP");
        }
        LOG.info("Discovery Service url found: " + discoveryServiceURL);

        DockerPort mongo = docker.containers().container("mongo").port(27017);
        mongoURL = String.format("http://%s:%s", mongo.getIp(), mongo.getExternalPort());
        while (!docker.containers().container("mongo").portIsListeningOnHttp(27017, (port) -> port.inFormat(mongoURL))
                .succeeded()) {
            LOG.info("Waiting for mongo to respond over HTTP");
        }
        LOG.info("Mongo url found: " + mongoURL);

        DockerPort userService = docker.containers().container("userservice").port(8080);
        userServiceURL = String.format("http://%s:%s", userService.getIp(), userService.getExternalPort());
        while (!docker.containers().container("userservice")
                .portIsListeningOnHttp(8080, (port) -> port.inFormat(userServiceURL)).succeeded()) {
            LOG.info("Waiting for user service to respond over HTTP");
        }
        LOG.info("User Service url found: " + userServiceURL);

        DockerPort tripManagementCommand = docker.containers().container("tripmanagementcmd").port(8080);
        tripCommandURL = String.format("http://%s:%s", tripManagementCommand.getIp(),
                tripManagementCommand.getExternalPort());
        while (!docker.containers().container("tripmanagementcmd")
                .portIsListeningOnHttp(8080, (port) -> port.inFormat(tripCommandURL)).succeeded()) {
            LOG.info("Waiting for Trip Command to respond over HTTP");
        }
        LOG.info("Trip Command url found: " + tripCommandURL);
    }

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private String token;

    @Before
    public void setUp() throws JSONException {
        String plainCreds = "front-end:front-end";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("username", "front-end");
        parameters.add("secret", "front-end");
        String body = "grant_type=password&scope=webclient&username=passenger&password=password";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // when:
        ResponseEntity<String> response = restTemplate.postForEntity(userServiceURL + "/auth/oauth/token", request,
                String.class, parameters);

        // then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        JSONObject json = new JSONObject(response.getBody());
        token = json.getString("access_token");
    }

    @Test
    public void tripCommandPOSTRequestSuccess() {
        // given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");

        String body = "{ \"originAddress\": \"Weston, FL\", \"destinationAddress\": "
                + "\"Miami, FL\", \"userId\": \"4eaf29bc-3909-49d4-a104-3d17f68ba672\" }";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // when:
        ResponseEntity<String> response = restTemplate.postForEntity(tripCommandURL + "/api/v1/trip", request,
                String.class);

        // then:
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

}
