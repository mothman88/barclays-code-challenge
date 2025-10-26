package com.barclays.challenge;

import com.barclays.challenge.model.Account;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.AccountRepository;
import com.barclays.challenge.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static com.barclays.challenge.TestFixture.createUser;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userEmail = "acctuser@example.com";
    private final String userPassword = "acctpass";
    private final String otherEmail = "other@example.com";
    private final String otherPassword = "otherpass";

    private User user;
    private User otherUser;


    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        accountRepository.deleteAll();
        userRepository.deleteAll();

        user = createUser(userEmail, userPassword);
        user = userRepository.save(user);

        otherUser = createUser(otherEmail, otherPassword);
        otherUser = userRepository.save(otherUser);
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
    public void createAccount_success_returnsCreated_andAccountDetails() throws Exception {
        String token = loginUser(userEmail, userPassword);

        Map<String, Object> payload = Map.of(
                "name", "Savings",
                "accountType", "SAV"
        );

        Response resp = RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(payload))
            .when()
                .post("/v1/accounts");

        resp.then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("accountNumber", notNullValue())
                .body("userId", equalTo(user.getId()))
                .body("name", equalTo("Savings"))
                .body("accountType", equalTo("SAV"));
    }

    @Test
    public void createAccount_missingData_returnsBadRequest_andError() throws Exception {
        String token = loginUser(userEmail, userPassword);

        // missing name/accountType
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(Map.of()))
            .when()
                .post("/v1/accounts")
            .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("name", equalTo("must not be null"));
    }

    @Test
    public void listAccounts_returnsAllAccountsForUser() throws Exception {
        // create two accounts for user and one for otherUser
        Account a1 = new Account();
        a1.setAccountNumber("A-1");
        a1.setUserId(user.getId());
        a1.setName("One");
        a1.setAccountType("CHK");
        accountRepository.save(a1);

        Account a2 = new Account();
        a2.setAccountNumber("A-2");
        a2.setUserId(user.getId());
        a2.setName("Two");
        a2.setAccountType("SAV");
        accountRepository.save(a2);

        Account other = new Account();
        other.setAccountNumber("B-1");
        other.setUserId(otherUser.getId());
        other.setName("Other");
        other.setAccountType("SAV");
        accountRepository.save(other);

        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/v1/accounts")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("userId", everyItem(equalTo(user.getId())));
    }

    @Test
    public void fetchAccount_ownAccount_returnsOk_andDetails() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-100");
        acc.setUserId(user.getId());
        acc.setName("MyAcc");
        acc.setAccountType("SAV");
        accountRepository.save(acc);

        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/v1/accounts/{accountId}", acc.getAccountNumber())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("accountNumber", equalTo(acc.getAccountNumber()))
                .body("userId", equalTo(user.getId()))
                .body("name", equalTo("MyAcc"));
    }

    @Test
    public void fetchAccount_otherUserAccount_returnsForbidden_andError() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-200");
        acc.setUserId(otherUser.getId());
        acc.setName("OtherAcc");
        acc.setAccountType("CHK");
        accountRepository.save(acc);

        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/v1/accounts/{accountId}", acc.getAccountNumber())
            .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("message", notNullValue());
    }

    @Test
    public void fetchAccount_nonExistent_returnsNotFound_andError() throws Exception {
        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/v1/accounts/{accountId}", "NO-SUCH-ACC")
            .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", notNullValue());
    }

    @Test
    public void updateAccount_ownAccount_updatesAndReturnsUpdated() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-300");
        acc.setUserId(user.getId());
        acc.setName("OldName");
        acc.setAccountType("CHK");
        accountRepository.save(acc);

        String token = loginUser(userEmail, userPassword);

        Map<String, Object> update = Map.of("name", "NewName", "accountType", "SAV");

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(update))
            .when()
                .patch("/v1/accounts/{accountId}", acc.getAccountNumber())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("accountNumber", equalTo(acc.getAccountNumber()))
                .body("name", equalTo("NewName"))
                .body("accountType", equalTo("SAV"));
    }

    @Test
    public void updateAccount_otherUser_returnsForbidden_andError() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-400");
        acc.setUserId(otherUser.getId());
        acc.setName("OtherOld");
        acc.setAccountType("CHK");
        accountRepository.save(acc);

        String token = loginUser(userEmail, userPassword);

        Map<String, Object> update = Map.of("name", "Attempt", "accountType", "Attempt");

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(update))
            .when()
                .patch("/v1/accounts/{accountId}", acc.getAccountNumber())
            .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("message", notNullValue());
    }

    @Test
    public void updateAccount_nonExistent_returnsNotFound_andError() throws Exception {
        String token = loginUser(userEmail, userPassword);

        Map<String, Object> update = Map.of("name", "Nope", "accountType", "Nope");

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(update))
            .when()
                .patch("/v1/accounts/{accountId}", "NO-ACC-999")
            .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", notNullValue());
    }

    @Test
    public void deleteAccount_ownAccount_deletesSuccessfully() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-500");
        acc.setUserId(user.getId());
        acc.setName("ToDelete");
        acc.setAccountType("SAV");
        accountRepository.save(acc);

        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete("/v1/accounts/{accountId}", acc.getAccountNumber())
            .then()
                .statusCode(204);

        assertTrue(accountRepository.findById(acc.getAccountNumber()).isEmpty());
    }

    @Test
    public void deleteAccount_otherUser_returnsForbidden_andError() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-600");
        acc.setUserId(otherUser.getId());
        acc.setName("OtherDelete");
        acc.setAccountType("CHK");
        accountRepository.save(acc);

        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete("/v1/accounts/{accountId}", acc.getAccountNumber())
            .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("message", notNullValue());
    }

    @Test
    public void deleteAccount_nonExistent_returnsNotFound_andError() throws Exception {
        String token = loginUser(userEmail, userPassword);

        RestAssured
            .given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete("/v1/accounts/{accountId}", "NON-EXISTENT")
            .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", notNullValue());
    }
}
