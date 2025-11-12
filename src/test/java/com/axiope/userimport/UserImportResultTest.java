package com.axiope.userimport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.researchspace.model.dto.UserRegistrationInfo;
import com.researchspace.model.field.ErrorList;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserImportResultTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testUserImportResultNoNullArgs1() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new UserImportResult(Collections.<UserRegistrationInfo>emptyList(), null, null, null);
        });
  }

  @Test
  public void testUserImportResultNoNullArgs2() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new UserImportResult(null, null, null, new ErrorList());
        });
  }

  @Test
  public void testUserImportResultGroupsCanBeNull() {
    UserImportResult result =
        new UserImportResult(
            Collections.<UserRegistrationInfo>emptyList(), null, null, new ErrorList());
    assertNotNull(result);
  }
}
