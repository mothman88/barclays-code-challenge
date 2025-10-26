# Barclays Code Challenge — API & Tests

Short description
- A Spring Boot REST API for a simple banking domain (users, bank accounts, transactions).
- Includes unit tests, JPA integration tests, and full end-to-end acceptance tests (RestAssured + optional Cucumber).
- Data persisted via JPA; migrations managed with Flyway. Tests run against an embedded test DB by default.
- Includes a Dockerfile and github action pipeline to season the project with some devops

## Tech stack
- Java (JDK 17+ recommended)
- Spring Boot (Web, Data JPA, Validation, Security)
- Docker
- Github action
- Flyway (DB migrations)
- H2 (in-memory DB for tests)
- Gradle (wrapper)
- Test libs: JUnit 5, Mockito, RestAssured, AssertJ

## Prerequisites
- Java 17+
- Git
- macOS / Linux / Windows with Gradle wrapper (./gradlew is included)

Build the project
- Clean & build (compile + unit tests + integration tests):
  ```./gradlew clean build```

Run the application locally
* Start the Spring Boot app:
  ```./gradlew bootRun```
* The app runs on the configured port (default 8080). Use the random-port profile when running acceptance tests.
* Access the swagger-ui http://localhost:8080/swagger-ui/index.html 

Run tests
- Run the full test suite (unit, integration, acceptance):
  ```./gradlew test```

## Manual Test

Access the swagger-ui interface http://localhost:8080/swagger-ui/index.html

1. Create a user invoking the [POST /v1/users](http://localhost:8080/swagger-ui/index.html#/user-controller/createUser) endpoint
```json
{
  "name": "string",
  "email": "myemail@test.com",
  "phoneNumber": "string",
  "address": {
    "line1": "string",
    "line2": "string",
    "town": "string",
    "county": "string",
    "postcode": "string"
  },
  "password": "password"
}
```

2. Invoke the login endpoint [POST /v1/auth/login](http://localhost:8080/swagger-ui/index.html#/auth-controller/login) with the username and password of your user
```json
{
  "email": "myemail@test.com",
  "password": "password"
}
```
it will return with a JWT token 
```json
{
  "token": "string"
}
```

3. use the generated token to invoke APIs, in the swagger-ui you'll need to click on the `Authorize` button and provide the token


Congratulation you can finally use the eagleye bank API

## improvements
* Database ids are all UUIDs
* Hash passwords at rest (currently stored clear text in DB for simplicity in tests).
* Adopt DDD / hexagonal architecture to separate domain, application, and infrastructure layers
* Expand Cucumber BDD features for broader acceptance coverage and product-readable scenarios.
* Use Testcontainers for DB realism and more reliable migration testing.
