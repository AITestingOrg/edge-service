package aist.edge.edgeservice;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.Container;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EdgeServiceIntegrationTests {
    protected static final Logger LOG = LoggerFactory.getLogger(EdgeServiceIntegrationTests.class);

    // private static String discoveryServiceURL;
    private static String mongoURL;
    private static String userServiceURL;
    private static String userServiceURL3;
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
    if (!discoveryPort.isListeningNow()) {
        LOG.info("Discovery service didn't respond over HTTP");
        throw new Exception(String.format("Discovery didn't respond, port: %s", discoveryPort.getInternalPort()));
    }
    LOG.info("Discovery service responded over HTTP");

    // Container mongoContainer = docker.containers().container("mongo");
    // DockerPort mongoPort = mongoContainer.port(27017);
    // mongoURL = String.format("http://%s:%s", mongoPort.getIp(),
    // mongoPort.getExternalPort());
    // if(!mongoPort.isListeningNow()){
    // LOG.info("Mongo service didn't respond over HTTP");
    // throw new Exception(String.format("Mongo didn't respond, port: %s",
    // mongoPort.getInternalPort()));
    // }
    // LOG.info("Mongo service responded over HTTP");
    //
    Container userContainer = docker.containers().container("userservice");
    DockerPort userPort = userContainer.port(8080);
    userServiceURL = String.format("http://%s:%s", userPort.getIp(), userPort.getExternalPort());
    if (!userPort.isListeningNow()) {
        LOG.info("User service didn't respond over HTTP");
        throw new Exception(String.format("User didn't respond, port: %s", userPort.getInternalPort()));
    }
    LOG.info("User service responded over HTTP");
    LOG.info(String.format("User Service docker-name: %s - IP: %s - Int Port: %s - Ext Port: %s",
        userContainer.getContainerName(), userPort.getIp(), userPort.getInternalPort(),
        userPort.getExternalPort()));

    Container tripManagementCmdContainer = docker.containers().container("tripmanagementcmd");
    DockerPort tripManagementCmdPort = tripManagementCmdContainer.port(8080);
    tripCommandURL = String.format("http://%s:%s", tripManagementCmdPort.getIp(),
        tripManagementCmdPort.getExternalPort());
    if (!tripManagementCmdPort.isListeningNow()) {
        LOG.info("TripManagementCmd service didn't respond over HTTP");
        throw new Exception(String.format("TripManagementCmd didn't respond, port: %s",
            tripManagementCmdPort.getInternalPort()));
    }
    LOG.info("TripManagementCmd service responded over HTTP");

    Container tripManagementQueryContainer = docker.containers().container("tripmanagementquery");
    DockerPort tripManagementQueryPort = tripManagementQueryContainer.port(8080);
    tripQueryURL = String.format("http://%s:%s", tripManagementQueryPort.getIp(),
        tripManagementQueryPort.getExternalPort());
    if (!tripManagementQueryPort.isListeningNow()) {
        LOG.info("TripManagementQuery service didn't respond over HTTP");
        throw new Exception(String.format("TripManagementQuery didn't respond, port: %s",
            tripManagementQueryPort.getInternalPort()));
    }
    LOG.info("TripManagementQuery service responded over HTTP");
    //
    // Container gmapsAdapterContainer =
    // docker.containers().container("gmapsadapter");
    // DockerPort gmapsAdapterPort = gmapsAdapterContainer.port(8080);
    // gmapsAdapterURL = String.format("http://%s:%s",
    // gmapsAdapterPort.getIp(),
    // gmapsAdapterPort.getExternalPort());
    // if(!gmapsAdapterPort.isListeningNow()){
    // LOG.info("Gmaps Adapter service didn't respond over HTTP");
    // throw new Exception(String.format("Gmaps Adapter didn't respond,
    // port: %s", gmapsAdapterPort.getInternalPort()));
    // }
    // LOG.info("Gmaps Adapter service responded over HTTP");
    //
    Container calculationContainer = docker.containers().container("calculationservice");
    DockerPort calculationPort = calculationContainer.port(8080);
    calculationServiceURL = String.format("http://%s:%s", calculationPort.getIp(),
        calculationPort.getExternalPort());
    if (!calculationPort.isListeningNow()) {
        LOG.info("Calculation service didn't respond over HTTP");
        throw new Exception(
            String.format("Calculation didn't respond, port: %s", calculationPort.getInternalPort()));
    }
    LOG.info("Calculation service responded over HTTP");
    //
    // DockerPort discoveryService =
    // docker.containers().container("discoveryservice").port(8761);
    // discoveryServiceURL = String.format("http://%s:%s",
    // discoveryService.getIp(),
    // discoveryService.getExternalPort());
    // while (!docker.containers().container("discoveryservice")
    // .portIsListeningOnHttp(8761, (port) ->
    // port.inFormat(discoveryServiceURL)).succeeded()) {
    // LOG.info("Waiting for discovery service to respond over HTTP");
    // }
    // LOG.info("Discovery Service url found: " + discoveryServiceURL);

    DockerPort mongo = docker.containers().container("mongo").port(27017);
    mongoURL = String.format("http://%s:%s", mongo.getIp(), mongo.getExternalPort());
    while (!docker.containers().container("mongo").portIsListeningOnHttp(27017, (port) -> port.inFormat(mongoURL))
        .succeeded()) {
        LOG.info("Waiting for mongo to respond over HTTP");
    }
    LOG.info("Mongo url found: " + mongoURL);

    // DockerPort userService =
    // docker.containers().container("userservice").port(8080);
    // userServiceURL = String.format("http://%s:%s", userService.getIp(),
    // userService.getExternalPort());
    // while (!docker.containers().container("userservice")
    // .portIsListeningOnHttp(8080, (port) ->
    // port.inFormat(userServiceURL)).succeeded()) {
    // LOG.info("Waiting for user service to respond over HTTP");
    // }
    // LOG.info("User Service url found: " + userServiceURL);

    // DockerPort tripManagementCommand =
    // docker.containers().container("tripmanagementcmd").port(8080);
    // tripCommandURL = String.format("http://%s:%s",
    // tripManagementCommand.getIp(),
    // tripManagementCommand.getExternalPort());
    // while (!docker.containers().container("tripmanagementcmd")
    // .portIsListeningOnHttp(8080, (port) ->
    // port.inFormat(tripCommandURL)).succeeded()) {
    // LOG.info("Waiting for Trip Command to respond over HTTP");
    // }
    // LOG.info("Trip Command url found: " + tripCommandURL);
    //
    // DockerPort tripManagementQuery =
    // docker.containers().container("tripmanagementquery").port(8080);
    // tripQueryURL = String.format("http://%s:%s",
    // tripManagementQuery.getIp(),
    // tripManagementQuery.getExternalPort());
    // while (!docker.containers().container("tripmanagementquery")
    // .portIsListeningOnHttp(8080, (port) ->
    // port.inFormat(tripQueryURL)).succeeded()) {
    // LOG.info("Waiting for Trip Query to respond over HTTP");
    // }
    // LOG.info("Trip Query url found: " + tripQueryURL);

    DockerPort gmapsAdapter = docker.containers().container("gmapsadapter").port(8080);
    gmapsAdapterURL = String.format("http://%s:%s", gmapsAdapter.getIp(), gmapsAdapter.getExternalPort());
    while (!docker.containers().container("gmapsadapter")
        .portIsListeningOnHttp(8080, (port) -> port.inFormat(gmapsAdapterURL)).succeeded()) {
        LOG.info("Waiting for user service to respond over HTTP");
    }
    LOG.info("Gmaps Adapter url found: " + gmapsAdapterURL);

    // DockerPort calculationService =
    // docker.containers().container("calculationservice").port(8080);
    // calculationServiceURL = String.format("http://%s:%s",
    // calculationService.getIp(),
    // calculationService.getExternalPort());
    // while (!docker.containers().container("calculationservice")
    // .portIsListeningOnHttp(8080, (port) ->
    // port.inFormat(calculationServiceURL)).succeeded()) {
    // LOG.info("Waiting for calculation service to respond over HTTP");
    // }
    // LOG.info("Calculation Service url found: " + calculationServiceURL);
    }

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private String token;

    @Before
    public void setUp() throws JSONException, IOException {
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
    ResponseEntity<String> response;

    //////////////////////////////////////////////

    //List<String> containerList = new ArrayList<String>();
    Runtime rt = Runtime.getRuntime();

    String[] commands = { "docker-machine", "env" };
    Process proc = rt.exec(commands);

    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

    // read the output from the command
    System.out.println("***************** Here is the standard output of the command:\n");
    String s = null;
    while ((s = stdInput.readLine()) != null) {
       // containerList.add(s);
        System.out.println(s);
        LOG.info(s);
    }

    // read any errors from the attempted command
    System.out.println("***************** Here is the standard error of the command (if any):\n");
    while ((s = stdError.readLine()) != null) {
        System.out.println(s);
        LOG.info(s);
    }


    String[] commands1 = { "docker", "info" };
    Process proc1 = rt.exec(commands1);

    BufferedReader stdInput1 = new BufferedReader(new InputStreamReader(proc1.getInputStream()));

    BufferedReader stdError1 = new BufferedReader(new InputStreamReader(proc1.getErrorStream()));

    // read the output from the command
    System.out.println("***************** Here is the standard output of the command:\n");
    String s1 = null;
    while ((s1 = stdInput1.readLine()) != null) {
        //containerList.add(s);
        System.out.println(s1);
        LOG.info(s1);
    }

    // read any errors from the attempted command
    System.out.println("***************** Here is the standard error of the command (if any):\n");
    while ((s1 = stdError1.readLine()) != null) {
        System.out.println(s1);
        LOG.info(s1);
    }
//
//    for (String containerId : containerList) {
//
//        String[] commands1 = { "docker", "inspect", containerId };
//        Process proc1 = rt.exec(commands1);
//
//        BufferedReader stdInput1 = new BufferedReader(new InputStreamReader(proc1.getInputStream()));
//
//        BufferedReader stdError1 = new BufferedReader(new InputStreamReader(proc1.getErrorStream()));
//
//        // read the output from the command
//        System.out.println("Here is the standard output of the command:\n");
//        String s1 = null;
//        while ((s1 = stdInput1.readLine()) != null) {
//        System.out.println(s1);
//        LOG.info(s1);
//        }
//
//        // read any errors from the attempted command
//        System.out.println("Here is the standard error of the command (if any):\n");
//        while ((s1 = stdError1.readLine()) != null) {
//        System.out.println(s1);
//        LOG.info(s1);
//        }
//    }
    //////////////////////////////////////////////

    LOG.info(String.format("User Service - Trying url: %s", userServiceURL));
    response = restTemplate.postForEntity(userServiceURL + "/auth/oauth/token", request, String.class, parameters);

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
            String.format("Expected 200, actual %s\n%s", response.getStatusCode(), response.getBody()));
    }

    // then:
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}
