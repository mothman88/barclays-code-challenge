package com.barclays.challenge;

import com.barclays.challenge.model.Account;
import com.barclays.challenge.model.Transaction;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.AccountRepository;
import com.barclays.challenge.repository.TransactionRepository;
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
import java.util.UUID;

import static com.barclays.challenge.TestFixture.createAccount;
import static com.barclays.challenge.TestFixture.createUser;
import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User alice;
    private User bob;
    private final String alicePassword = "alicepass";
    private final String bobPassword = "bobpass";

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        alice = createUser("alice@example.com", alicePassword);
        alice = userRepository.save(alice);

        bob = createUser("bob@example.com", bobPassword);
        bob = userRepository.save(bob);
    }

    private String loginUser(String email, String password) throws Exception {
        Map<String, String> login = Map.of("email", email, "password", password);
        Response loginResp = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(login))
                .post("/v1/auth/login");

        loginResp.then().statusCode(200);
        return loginResp.jsonPath().getString("token");
    }

    @Test
    public void deposit_into_own_account_registers_transaction_and_updates_balance() throws Exception {
        Account acc = createAccount("ACC-DEP-1", alice.getId());
        acc.setBalance(100);
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        Map<String, Object> txReq = Map.of("type", "deposit", "amount", 50.0);

        Response resp = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(txReq))
                .post("/v1/accounts/{accountId}/transactions", acc.getAccountNumber());

        resp.then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("accountNumber", equalTo(acc.getAccountNumber()))
                .body("type", equalTo("deposit"))
                .body("amount", equalTo(50.0f));

        // verify transaction persisted and balance updated
        var txs = transactionRepository.findByAccountNumber(acc.getAccountNumber());
        assertThat(txs).hasSize(1);
        Account updated = accountRepository.findById(acc.getAccountNumber()).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(150.0);
    }

    @Test
    public void withdrawal_with_sufficient_funds_registers_transaction_and_updates_balance() throws Exception {
        Account acc = createAccount("ACC-WITH-1", alice.getId());
        acc.setBalance(200.0);
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        Map<String, Object> txReq = Map.of("type", "withdrawal", "amount", 80.0);

        Response resp = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(txReq))
                .post("/v1/accounts/{accountId}/transactions", acc.getAccountNumber());

        resp.then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("type", equalTo("withdrawal"))
                .body("amount", equalTo(80.0f));

        var txs = transactionRepository.findByAccountNumber(acc.getAccountNumber());
        assertThat(txs).hasSize(1);
        Account updated = accountRepository.findById(acc.getAccountNumber()).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(120.0);
    }

    @Test
    public void withdrawal_with_insufficient_funds_returns_unprocessable_and_error() throws Exception {
        Account acc = createAccount("ACC-WITH-INSUF", alice.getId());
        acc.setBalance(30.0);
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        Map<String, Object> txReq = Map.of("type", "withdrawal", "amount", 100.0);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(txReq))
        .when()
            .post("/v1/accounts/{accountId}/transactions", acc.getAccountNumber())
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("message", not(isEmptyString()));
    }

    @Test
    public void transaction_on_another_users_account_returns_forbidden_and_error() throws Exception {
        Account acc = createAccount("ACC-OTH-1", bob.getId());
        acc.setBalance(500.0);
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        Map<String, Object> txReq = Map.of("type", "deposit", "amount", 10.0);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(txReq))
        .when()
            .post("/v1/accounts/{accountId}/transactions", acc.getAccountNumber())
        .then()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

    @Test
    public void transaction_on_nonexistent_account_returns_not_found_and_error() throws Exception {
        String token = loginUser(alice.getEmail(), alicePassword);

        Map<String, Object> txReq = Map.of("type", "deposit", "amount", 10.0);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(txReq))
        .when()
            .post("/v1/accounts/{accountId}/transactions", "NO-SUCH-ACC")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

    @Test
    @Disabled("need to double check validation errors")
    public void transaction_missing_required_data_returns_bad_request_and_error() throws Exception {
        Account acc = createAccount("ACC-MISS-REQ", alice.getId());
        acc.setBalance(50.0);
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        // missing type/amount => empty body
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"field1\":\"value1\"}")
        .when()
            .post("/v1/accounts/{accountId}/transactions", acc.getAccountNumber())
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("message", not(isEmptyString()));
    }

    @Test
    public void list_transactions_for_own_account_returns_transactions() throws Exception {
        Account acc = createAccount("ACC-LIST-1", alice.getId());
        acc.setBalance(0.0);
        accountRepository.save(acc);

        Transaction t1 = new Transaction();
        t1.setId(UUID.randomUUID().toString());
        t1.setAccountNumber(acc.getAccountNumber());
        t1.setUserId(alice.getId());
        t1.setType("deposit");
        t1.setAmount(10.0);
        transactionRepository.save(t1);

        Transaction t2 = new Transaction();
        t2.setId(UUID.randomUUID().toString());
        t2.setAccountNumber(acc.getAccountNumber());
        t2.setUserId(alice.getId());
        t2.setType("withdrawal");
        t2.setAmount(5.0);
        transactionRepository.save(t2);

        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions", acc.getAccountNumber())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", is(2));
    }

    @Test
    public void list_transactions_for_other_user_account_returns_forbidden_and_error() throws Exception {
        Account acc = createAccount("ACC-LIST-OTH", bob.getId());
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions", acc.getAccountNumber())
        .then()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

    @Test
    public void list_transactions_for_nonexistent_account_returns_not_found_and_error() throws Exception {
        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions", "NO-ACC-123")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

    @Test
    public void fetch_transaction_on_own_account_returns_transaction() throws Exception {
        Account acc = createAccount("1234567890", alice.getId());
        accountRepository.save(acc);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setAccountNumber(acc.getAccountNumber());
        tx.setUserId(alice.getId());
        tx.setType("deposit");
        tx.setAmount(20.0);
        tx = transactionRepository.save(tx);

        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions/{transactionId}", acc.getAccountNumber(), tx.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(tx.getId()))
            .body("accountNumber", equalTo(acc.getAccountNumber()));
    }

    @Test
    public void fetch_transaction_on_other_user_account_returns_forbidden_and_error() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-FETCH-OTH");
        acc.setName("ACC-FETCH-OTH");
        acc.setAccountType("personal");
        acc.setUserId(bob.getId());
        accountRepository.save(acc);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setAccountNumber(acc.getAccountNumber());
        tx.setUserId(bob.getId());
        tx.setType("deposit");
        tx.setAmount(10.0);
        tx = transactionRepository.save(tx);

        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions/{transactionId}", acc.getAccountNumber(), tx.getId())
        .then()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

    @Test
    public void fetch_transaction_on_nonexistent_account_returns_not_found_and_error() throws Exception {
        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions/{transactionId}", "NO-ACC", "TX-1")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

    @Test
    public void fetch_nonexistent_transaction_on_own_account_returns_not_found_and_error() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("ACC-FETCH-NULL");
        acc.setUserId(alice.getId());
        acc.setAccountType("personal");
        acc.setName("acc-name");
        accountRepository.save(acc);

        String token = loginUser(alice.getEmail(), alicePassword);

        given()
            .header("Authorization", "Bearer " + token)
            .accept(ContentType.JSON)
        .when()
            .get("/v1/accounts/{accountId}/transactions/{transactionId}", acc.getAccountNumber(), "NO-TX")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", notNullValue());
    }

}
