package com.splitscale.integration;

import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class ShieldApiTest {
  private String userId;
  private String jwtToken;

  @BeforeAll
  public static void setup() {
    // Set the base URL and port for the API server
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 8080;
  }

  @Test
  @Order(1)
  public void testLoginUser_Success() {
    Response response = given()
        .filter(new ErrorLoggingFilter())
        .contentType(ContentType.JSON)
        .body("{\"username\":\"joejoe\",\"password\":\"password\"}")
        .when()
        .post("/api/v1/login")
        .then()
        .statusCode(200)
        .body("token", notNullValue())
        .extract()
        .response();

    // Store the userId and jwtToken values for use in subsequent tests
    userId = response.path("userResponse.id");
    jwtToken = response.path("token");
  }

  @Test
  @Order(2)
  public void testRegisterUser_Success() {
    Assumptions.assumeTrue(userId != null && jwtToken != null, "Previous test case failed.");

    given()
        .filter(new ErrorLoggingFilter())
        .contentType(ContentType.JSON)
        .body("{\"username\":\"joejoe\",\"password\":\"password\"}")
        .when()
        .post("/api/v1/register")
        .then()
        .statusCode(200)
        .log().all();
  }

  @Test
  @Order(3)
  public void testValidateJwt_Success() {
    Assumptions.assumeTrue(userId != null && jwtToken != null, "Previous test case failed.");

    System.out.println(userId);
    System.out.println(jwtToken);

    given()
        .contentType(ContentType.JSON)
        .body("{\"jwtToken\":\"" + jwtToken + "\",\"userId\":\"" + userId + "\"}")
        .when()
        .post("/api/v1/validateJwt")
        .then()
        .statusCode(200)
        .body("token", notNullValue())
        .log().all();
  }

  @Test
  @Order(4)
  public void testInValidateJwt_Success() {
    Assumptions.assumeTrue(userId != null && jwtToken != null, "Previous test case failed.");

    System.out.println(userId);
    System.out.println(jwtToken);

    given()
        .body(jwtToken)
        .when()
        .post("/api/v1/inValidateJwt")
        .then()
        .statusCode(200)
        .body(notNullValue())
        .log().all();
  }
}
