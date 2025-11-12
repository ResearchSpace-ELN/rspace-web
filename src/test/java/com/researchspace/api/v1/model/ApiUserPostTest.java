package com.researchspace.api.v1.model;

import com.researchspace.core.testutil.JavaxValidatorTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApiUserPostTest extends JavaxValidatorTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testValidation() {
    // start off with valid user
    ApiUserPost userToCreate =
        ApiUserPost.builder()
            .email("x@y.com")
            .username("abcdefg")
            .firstName("bob")
            .lastName("smith")
            .password("12312312")
            .role("ROLE_USER")
            .build();
    assertValid(userToCreate);
    userToCreate.setRole("ROLE_PI");
    assertValid(userToCreate);

    userToCreate.setFirstName("   ");
    assertNErrors(userToCreate, 1);
    userToCreate.setLastName("  ");
    assertNErrors(userToCreate, 2);

    userToCreate.setEmail("xxxxx"); // invalid
    assertNErrors(userToCreate, 3);

    userToCreate.setPassword("222"); // too short
    assertNErrors(userToCreate, 5);

    userToCreate.setPassword("2222       ");
    assertNErrors(userToCreate, 4);
  }
}
