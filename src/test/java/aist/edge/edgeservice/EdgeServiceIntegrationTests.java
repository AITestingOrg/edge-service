package aist.edge.edgeservice;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.Container;
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
public class EdgeServiceIntegrationTests {
    protected static final Logger LOG = LoggerFactory.getLogger(EdgeServiceIntegrationTests.class);

    private static String discoveryServiceURL;
    private static String mongoURL;
    private static String userServiceURL;
    private static String tripCommandURL;
    private static String tripQueryURL;
    private static String gmapsAdapterURL;
    private static String calculationServiceURL;

    // Wait for all services to have ports open
    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("discoveryservice", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("mysqlserver", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("mongo", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("userservice", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("tripmanagementcmd", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("tripmanagementquery", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("gmapsadapter", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("calculationservice", HealthChecks.toHaveAllPortsOpen()).build();

    // Get IP addresses and ports to run tests on
    @BeforeClass
    public static void initialize() throws Exception {

        LOG.info("Initializing ports from Docker");

        Container discoveryContainer = docker.containers().container("discoveryservice");
        DockerPort discoveryPort = discoveryContainer.port(8761);
        discoveryServiceURL = String.format("http://%s:%s", discoveryPort.getIp(),
        	discoveryPort.getExternalPort());
        if(!discoveryPort.isListeningNow()){
            LOG.info("Discovery service didn't respond over HTTP");
            throw new Exception(String.format("Discovery didn't respond, port: %s", discoveryPort.getInternalPort()));
        }
        LOG.info("Discovery service responded over HTTP");

        Container mongoContainer = docker.containers().container("mongo");
        DockerPort mongoPort = mongoContainer.port(27017);
        mongoURL = String.format("http://%s:%s", mongoPort.getIp(), mongoPort.getExternalPort());
        if(!mongoPort.isListeningNow()){
            LOG.info("Mongo service didn't respond over HTTP");
            throw new Exception(String.format("Mongo didn't respond, port: %s", mongoPort.getInternalPort()));
        }
        LOG.info("Mongo service responded over HTTP");

        Container userContainer = docker.containers().container("userservice");
        DockerPort userPort = userContainer.port(8080);
        userServiceURL = String.format("http://%s:%s", userPort.getIp(), userPort.getExternalPort());
        if(!userPort.isListeningNow()){
            LOG.info("User service didn't respond over HTTP");
            throw new Exception(String.format("User didn't respond, port: %s", userPort.getInternalPort()));
        }
        LOG.info("User service responded over HTTP");

        Container tripManagementCmdContainer = docker.containers().container("tripmanagementcmd");
        DockerPort tripManagementCmdPort = tripManagementCmdContainer.port(8080);
        tripCommandURL = String.format("http://%s:%s", tripManagementCmdPort.getIp(),
        	tripManagementCmdPort.getExternalPort());
        if(!tripManagementCmdPort.isListeningNow()){
            LOG.info("TripManagementCmd service didn't respond over HTTP");
            throw new Exception(String.format("TripManagementCmd didn't respond, port: %s", tripManagementCmdPort.getInternalPort()));
        }
        LOG.info("TripManagementCmd service responded over HTTP");

        Container tripManagementQueryContainer = docker.containers().container("tripmanagementquery");
        DockerPort tripManagementQueryPort = tripManagementQueryContainer.port(8080);
        tripQueryURL = String.format("http://%s:%s", tripManagementQueryPort.getIp(),
        	tripManagementQueryPort.getExternalPort());
        if(!tripManagementQueryPort.isListeningNow()){
            LOG.info("TripManagementQuery service didn't respond over HTTP");
            throw new Exception(String.format("TripManagementQuery didn't respond, port: %s", tripManagementQueryPort.getInternalPort()));
        }
        LOG.info("TripManagementQuery service responded over HTTP");

        Container gmapsAdapterContainer = docker.containers().container("gmapsadapter");
        DockerPort gmapsAdapterPort = gmapsAdapterContainer.port(8080);
        gmapsAdapterURL = String.format("http://%s:%s", gmapsAdapterPort.getIp(),
        	gmapsAdapterPort.getExternalPort());
        if(!gmapsAdapterPort.isListeningNow()){
            LOG.info("Gmaps Adapter service didn't respond over HTTP");
            throw new Exception(String.format("Gmaps Adapter didn't respond, port: %s", gmapsAdapterPort.getInternalPort()));
        }
        LOG.info("Gmaps Adapter service responded over HTTP");

        Container calculationContainer = docker.containers().container("calculationservice");
        DockerPort calculationPort = calculationContainer.port(8080);
        calculationServiceURL = String.format("http://%s:%s", calculationPort.getIp(),
        	calculationPort.getExternalPort());
        if(!calculationPort.isListeningNow()){
            LOG.info("Calculation service didn't respond over HTTP");
            throw new Exception(String.format("Calculation didn't respond, port: %s", calculationPort.getInternalPort()));
        }
        LOG.info("Calculation service responded over HTTP");
        
        LOG.info("Containers initialized correctly");
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

    @Test
    public void tripQueryGETSpecificTripRequestSuccess() throws JSONException, InterruptedException {
        // given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");

        String body = "{ \"originAddress\": \"Weston, FL\", \"destinationAddress\": "
                + "\"Miami, FL\", \"userId\": \"4eaf29bc-3909-49d4-a104-3d17f68ba672\" }";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> postResponse = restTemplate.postForEntity(tripCommandURL + "/api/v1/trip", request,
                String.class);
        
        assertThat(postResponse.getStatusCodeValue()).isEqualTo(201);

        JSONObject json = new JSONObject(postResponse.getBody());
        String tripId = json.getString("id");
        
        Thread.sleep(1000);

        // when:
        ResponseEntity<String> response = restTemplate.getForEntity(tripQueryURL + "/api/v1/trip/" + tripId,
                String.class);

        // then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void tripQueryGETAllTripsRequestSuccess() {
        // given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");

        // when:
        ResponseEntity<String> response = restTemplate.getForEntity(tripQueryURL + "/api/v1/trips", String.class);

        // then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void calculationServiceRequestSuccess() {
        // given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");

        String body = "{ \"origin\": \"Weston, FL\", \"destination\": \"Miami, FL\","
                + " \"userId\": \"4eaf29bc-3909-49d4-a104-3d17f68ba672\" }";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // when:
        ResponseEntity<String> response = restTemplate.postForEntity(calculationServiceURL + "/api/v1/cost", request,
                String.class);

        // then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void gmapsAdapterRequestSuccess() throws Exception {
        // given:
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");

        String body = "{ \"origin\": \"2250 N Commerce Pkwy, Weston, FL 33326\", \"destination\": \"11200 SW 8th St, "
                + "Miami, FL 33199\", \"departureTime\": \"15220998650000000\" }";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // when:
        ResponseEntity<String> response = restTemplate.postForEntity(gmapsAdapterURL + "/api/v1/directions", request,
                String.class);

        if (response.getStatusCodeValue() != 200) {
            throw new Exception(
                    String.format("Expected 200, actual %s\n%s", response.getStatusCode(),
                            response.getBody()));
        }

        // then:
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}
