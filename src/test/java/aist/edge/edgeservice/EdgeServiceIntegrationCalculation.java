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
//public class EdgeServiceIntegrationCalculation {
//    protected static final Logger LOG = LoggerFactory.getLogger(EdgeServiceIntegrationCalculation.class);
//
//    private static String discoveryServiceURL;
//    private static String mongoURL;
//    private static String gmapsAdapterURL;
//    private static String calculationServiceURL;
//    private static Duration DEFAULT_TIMEOUT = Duration.standardMinutes(2);
//
//    // Wait for all services to have ports open
//    @ClassRule
//    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
//            .file("src/test/resources/docker-compose-calculation.yml").shutdownStrategy(ShutdownStrategy.KILL_DOWN)
//            .waitingForService("discoveryservice", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .waitingForService("mongo", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .waitingForService("gmapsadapter", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
//            .waitingForService("calculationservice", HealthChecks.toHaveAllPortsOpen(), DEFAULT_TIMEOUT)
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
//        Container mongoContainer = docker.containers().container("mongo");
//        DockerPort mongoPort = mongoContainer.port(27017);
//        mongoURL = String.format("http://%s:%s", mongoPort.getIp(), mongoPort.getExternalPort());
//        if(!mongoPort.isListeningNow()){
//            LOG.info("Mongo service didn't respond over HTTP");
//            throw new Exception(String.format("Mongo didn't respond, port: %s", mongoPort.getInternalPort()));
//        }
//        LOG.info("Mongo service responded over HTTP");
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
//        Container calculationContainer = docker.containers().container("calculationservice");
//        DockerPort calculationPort = calculationContainer.port(8080);
//        calculationServiceURL = String.format("http://%s:%s", calculationPort.getIp(),
//        	calculationPort.getExternalPort());
//        if(!calculationPort.isListeningNow()){
//            LOG.info("Calculation service didn't respond over HTTP");
//            throw new Exception(String.format("Calculation didn't respond, port: %s", calculationPort.getInternalPort()));
//        }
//        LOG.info("Calculation service responded over HTTP");
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
//    public void calculationServiceRequestSuccess() {
//        // given:
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + token);
//        headers.add("Content-Type", "application/json");
//
//        String body = "{ \"origin\": \"Weston, FL\", \"destination\": \"Miami, FL\","
//                + " \"userId\": \"4eaf29bc-3909-49d4-a104-3d17f68ba672\" }";
//        HttpEntity<String> request = new HttpEntity<>(body, headers);
//
//        // when:
//        ResponseEntity<String> response = restTemplate.postForEntity(calculationServiceURL + "/api/v1/cost", request,
//                String.class);
//
//        // then:
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//    }
//}
