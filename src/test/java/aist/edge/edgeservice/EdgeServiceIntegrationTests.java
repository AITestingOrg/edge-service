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
    private static String tripCommandURL;
    private static String tripQueryURL;
    private static String gmapsAdapterURL;
    private static String calculationServiceURL;

  //Wait for all services to have ports open
    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().pullOnStartup(true)
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("userservice", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("mongo", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("rabbitmq", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("tripmanagementcmd", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("tripmanagementquery", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("gmapsadapter", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("calculationservice", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("discoveryservice", HealthChecks.toHaveAllPortsOpen())
//            .waitingForService("discoveryservice", HealthChecks.toRespondOverHttp(8761,
//                (port) -> port.inFormat("http://$HOST:$EXTERNAL_PORT")))
            .build();

    //Get IP addresses and ports to run tests on
    @BeforeClass
    public static void initialize() {
        LOG.info("Initializing ports from Docker");
        DockerPort tripManagementCommand = docker.containers().container("tripmanagementcmd")
                .port(8080);
        tripCommandURL = String.format("http://%s:%s", tripManagementCommand.getIp(),
                tripManagementCommand.getExternalPort());
        LOG.info("Trip Command url found: " + tripCommandURL);

        DockerPort tripManagementQuery = docker.containers().container("tripmanagementquery")
                .port(8080);
        tripQueryURL = String.format("http://%s:%s", tripManagementQuery.getIp(),
                tripManagementQuery.getExternalPort());
        LOG.info("Trip Query url found: " + tripQueryURL);

        DockerPort gmapsAdapter = docker.containers().container("gmapsadapter")
                .port(8080);
        gmapsAdapterURL = String.format("http://%s:%s", gmapsAdapter.getIp(),
                gmapsAdapter.getExternalPort());
        LOG.info("Gmaps Adapter url found: " + gmapsAdapterURL);

        DockerPort calculationService = docker.containers().container("calculationservice")
                .port(8080);
        calculationServiceURL = String.format("http://%s:%s", calculationService.getIp(),
                calculationService.getExternalPort());
        while (!docker.containers().container("calculationservice").portIsListeningOnHttp(8080,
            (port) -> port.inFormat(calculationServiceURL)).succeeded()) {
            LOG.info("Waiting for calculation service to respond over HTTP");
        }
        LOG.info("Calculation Service url found: " + calculationServiceURL);

        DockerPort userService = docker.containers().container("userservice")
                .port(8080);
        userServiceURL = String.format("http://%s:%s", userService.getIp(),
                userService.getExternalPort());
        while (!docker.containers().container("userservice").portIsListeningOnHttp(8080,
            (port) -> port.inFormat(userServiceURL)).succeeded()) {
            LOG.info("Waiting for user service to respond over HTTP");
        }
        LOG.info("User Service url found: " + userServiceURL);
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
        
         String[] commands = { "netstat", "-an" };
         Process proc = rt.exec(commands);
        
         BufferedReader stdInput = new BufferedReader(new
         InputStreamReader(proc.getInputStream()));
        
         BufferedReader stdError = new BufferedReader(new
         InputStreamReader(proc.getErrorStream()));
        
         // read the output from the command
         System.out.println("****** Here is the standard output of the command:\n");
         String s = null;
         while ((s = stdInput.readLine()) != null) {
         // containerList.add(s);
         System.out.println(s);
         LOG.info(s);
         }
        
         // read any errors from the attempted command
         System.out.println("****** Here is the standard error of the command (if any):\n");
         while ((s = stdError.readLine()) != null) {
         System.out.println(s);
         LOG.info(s);
         }
        //
        // for (String containerId : containerList) {
        //
        // String[] commands1 = { "docker", "inspect", containerId };
        // Process proc1 = rt.exec(commands1);
        //
        // BufferedReader stdInput1 = new BufferedReader(new
        // InputStreamReader(proc1.getInputStream()));
        //
        // BufferedReader stdError1 = new BufferedReader(new
        // InputStreamReader(proc1.getErrorStream()));
        //
        // // read the output from the command
        // System.out.println("Here is the standard output of the command:\n");
        // String s1 = null;
        // while ((s1 = stdInput1.readLine()) != null) {
        // System.out.println(s1);
        // LOG.info(s1);
        // }
        //
        // // read any errors from the attempted command
        // System.out.println("Here is the standard error of the command (if
        // any):\n");
        // while ((s1 = stdError1.readLine()) != null) {
        // System.out.println(s1);
        // LOG.info(s1);
        // }
        // }
        //////////////////////////////////////////////

        LOG.info(String.format("User Service - Trying url: %s", userServiceURL));
        response = restTemplate.postForEntity(userServiceURL + "/auth/oauth/token", request, String.class, parameters);
        LOG.info("Request succeded");

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
