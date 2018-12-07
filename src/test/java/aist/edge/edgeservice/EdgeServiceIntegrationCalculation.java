package aist.edge.edgeservice;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import java.util.*;

import org.joda.time.Duration;
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
public class EdgeServiceIntegrationCalculation {
    protected static final Logger LOG = LoggerFactory.getLogger(EdgeServiceIntegrationCalculation.class);

    private static String discoveryServiceURL;
    private static String mongoURL;
    private static String gmapsAdapterURL;
    private static String calculationServiceURL;
    private static Duration DEFAULT_TIMEOUT = Duration.standardMinutes(2);

    // Wait for all services to have ports open
    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
            .file("src/test/resources/docker-compose-calculation.yml").shutdownStrategy(ShutdownStrategy.KILL_DOWN)
            .waitingForService("discoveryservice", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
            .waitingForService("mongo", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
            .waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
            .waitingForService("gmapsadapter", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
            .waitingForService("calculationservice", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
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

        DockerPort gmapsAdapter = docker.containers().container("gmapsadapter").port(8080);
        gmapsAdapterURL = String.format("http://%s:%s", gmapsAdapter.getIp(), gmapsAdapter.getExternalPort());
        while (!docker.containers().container("gmapsadapter")
                .portIsListeningOnHttp(8080, (port) -> port.inFormat(gmapsAdapterURL)).succeeded()) {
            LOG.info("Waiting for gmapsadapter to respond over HTTP");
        }
        LOG.info("Gmaps Adapter url found: " + gmapsAdapterURL);

        DockerPort calculationService = docker.containers().container("calculationservice").port(8080);
        calculationServiceURL = String.format("http://%s:%s", calculationService.getIp(),
                calculationService.getExternalPort());
        while (!docker.containers().container("calculationservice")
                .portIsListeningOnHttp(8080, (port) -> port.inFormat(calculationServiceURL)).succeeded()) {
            LOG.info("Waiting for calculation service to respond over HTTP");
        }
        LOG.info("Calculation Service url found: " + calculationServiceURL);
    }

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private String token;

    @Before
    public void setUp() throws JSONException {
        token = "";
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
}
