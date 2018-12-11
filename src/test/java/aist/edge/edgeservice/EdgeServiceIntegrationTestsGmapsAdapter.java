//package aist.edge.edgeservice;
//
//import static org.assertj.core.api.Java6Assertions.assertThat;
//
//import com.palantir.docker.compose.DockerComposeRule;
//import com.palantir.docker.compose.configuration.ShutdownStrategy;
//import com.palantir.docker.compose.connection.Container;
//import com.palantir.docker.compose.connection.DockerPort;
//import com.palantir.docker.compose.connection.waiting.HealthChecks;
//
//import java.util.*;
//
//import org.joda.time.Duration;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.ClassRule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.*;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.util.*;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//public class EdgeServiceIntegrationTestsGmapsAdapter {
//    protected static final Logger LOG = LoggerFactory.getLogger(EdgeServiceIntegrationTestsGmapsAdapter.class);
//
//    private static String discoveryServiceURL;
//    private static String gmapsAdapterURL;
//    private static Duration DEFAULT_TIMEOUT = Duration.standardMinutes(2);
//
//    // Wait for all services to have ports open
//    @ClassRule
//    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
//            .file("src/test/resources/docker-compose-gmaps-adapter.yml").shutdownStrategy(ShutdownStrategy.KILL_DOWN)
//            .waitingForService("discoveryservice", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .waitingForService("gmapsadapter", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .build();
//
//    // Get IP addresses and ports to run tests on
//    @BeforeClass
//    public static void initialize() throws Exception {
//
//        LOG.info("Initializing ports from Docker");
//
//        Container discoveryContainer = docker.containers().container("discoveryservice");
//        DockerPort discoveryPort = discoveryContainer.port(8761);
//        discoveryServiceURL = String.format("http://%s:%s", discoveryPort.getIp(),
//        	discoveryPort.getExternalPort());
//        if(!discoveryPort.isListeningNow()){
//            LOG.info("Discovery service didn't respond over HTTP");
//            throw new Exception(String.format("Discovery didn't respond, port: %s", discoveryPort.getInternalPort()));
//        }
//        LOG.info("Discovery service responded over HTTP");
//
//        Container gmapsAdapterContainer = docker.containers().container("gmapsadapter");
//        DockerPort gmapsAdapterPort = gmapsAdapterContainer.port(8080);
//        gmapsAdapterURL = String.format("http://%s:%s", gmapsAdapterPort.getIp(),
//        	gmapsAdapterPort.getExternalPort());
//        if(!gmapsAdapterPort.isListeningNow()){
//            LOG.info("Gmaps Adapter service didn't respond over HTTP");
//            throw new Exception(String.format("Gmaps Adapter didn't respond, port: %s", gmapsAdapterPort.getInternalPort()));
//        }
//        LOG.info("Gmaps Adapter service responded over HTTP");
//        
//        LOG.info("Containers initialized correctly");
//    }
//
//    private TestRestTemplate restTemplate = new TestRestTemplate();
//
//    private String token;
//
//    @Before
//    public void setUp() throws JSONException {
//        token = "";
//    }
//
//    @Test
//    public void gmapsAdapterRequestSuccess() throws Exception {
//        // given:
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + token);
//        headers.add("Content-Type", "application/json");
//
//        String body = "{ \"origin\": \"2250 N Commerce Pkwy, Weston, FL 33326\", \"destination\": \"11200 SW 8th St, "
//                + "Miami, FL 33199\", \"departureTime\": \"15220998650000000\" }";
//        HttpEntity<String> request = new HttpEntity<>(body, headers);
//
//        // when:
//        ResponseEntity<String> response = restTemplate.postForEntity(gmapsAdapterURL + "/api/v1/directions", request,
//                String.class);
//
//        if (response.getStatusCodeValue() != 200) {
//            throw new Exception(
//                    String.format("Expected 200, actual %s\n%s", response.getStatusCode(),
//                            response.getBody()));
//        }
//
//        // then:
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//    }
//}
