package com.splitscale.integration;

import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
  public void testRegisterUser() {
    Response response = given()
        .filter(new ErrorLoggingFilter())
        .contentType(ContentType.JSON)
        .body("{\"username\":\"joejoe\",\"password\":\"password\"}")
        .when()
        .post("/api/v1/register")
        .then()
        .log().all()
        .extract()
        .response();

    // Check the status code and skip the test if it's 409
    int statusCode = response.statusCode();
    if (statusCode == 409) {
      System.out.println("Skipping Register User due to status code 409");
      return;
    }

    // Assert the status code is 200
    assertEquals(200, statusCode);

    // Add additional assertions if needed
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  @Order(2)
  public void testLoginUser_Success() {
    Response response = given()
        .filter(new ErrorLoggingFilter())
        .contentType(ContentType.JSON)
        .body("{\"username\":\"joejoe\",\"password\":\"password\"}")
        .when()
        .post("/api/v1/login")
        .then()
        .statusCode(200)
        .log().all()
        .header("token", notNullValue())
        .extract()
        .response();

    // Store the userId and jwtToken values for use in subsequent tests
    userId = response.path("id");
    jwtToken = response.header("token");
  }

  @Test
  @Order(3)
  public void testValidateJwt_Success() {
    assumeTrue(userId != null && jwtToken != null, "Previous test case failed.");

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
    assumeTrue(userId != null && jwtToken != null, "Previous test case failed.");

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
