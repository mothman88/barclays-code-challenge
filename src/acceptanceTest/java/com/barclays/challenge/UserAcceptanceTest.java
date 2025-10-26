package com.barclays.challenge;

import com.barclays.challenge.model.Address;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.barclays.challenge.TestFixture.createUser;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String alicePassword = "securepassword";
    private final String aliceEmail = "email@barclays.com";
    private final String bobPassword = "securepassword";
    private final String bobEmail = "email2@barclays.com";
    private User alice;
    private User bob;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;

        userRepository.deleteAll();

        alice = createUser(aliceEmail, alicePassword);
        alice = userRepository.save(alice);

        bob = createUser(bobEmail, bobPassword);
        bob = userRepository.save(bob);
    }

    private String loginUser(String email, String password) throws Exception {
        Map<String, String> login = Map.of("email", email, "password", password);
        Response loginResp = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(login))
                .post("/v1/auth/login");

        loginResp.then().statusCode(200);
        return loginResp.jsonPath().getString("token");
    }

    @Test
    public void shouldRetrieveUser() throws Exception {
        // 1) authenticate -> obtain token
        String token = loginUser(aliceEmail, alicePassword);

        // 2) request user details with Authorization header
        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/v1/users/{userId}", alice.getId())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("email", equalTo(aliceEmail))
                .body("phoneNumber", equalTo("+07777777777"));
    }

    @Test
    public void authenticatedUser_getNonexistentUser_returnsNotFound_andErrorMessage() throws Exception {
        // authenticate -> obtain token
        String token = loginUser(aliceEmail, alicePassword);

        // request non-existent user id
        String missingId = "non-existent-id-123";

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/v1/users/{userId}", missingId)
            .then()
                .statusCode(404);
    }

    @Test
    public void createNewUser() throws Exception {
        // choose a new email to avoid collisions with setUp persistedUser
        User user = User.builder()
            .id(UUID.randomUUID().toString())
            .name("user-" + new Random().nextInt(99999))
            .email("email")
            .password("password")
            .phoneNumber("+07777777777")
            .address(Address.builder()
                .line1("line1")
                .line2("line2")
                .county("county")
                .town("town")
                .postcode("postcode")
                .build())
            .build();

        // POST /v1/users (signup) - no auth required
        Response createResp = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(user))
                .post("/v1/users");

        // Assert created response and returned body contains id + fields
        createResp.then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("email", equalTo("email"))
                .body("address.line1", equalTo("line1"));
    }
}