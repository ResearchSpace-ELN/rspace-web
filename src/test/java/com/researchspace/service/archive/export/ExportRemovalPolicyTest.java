package com.researchspace.service.archive.export;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.record.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExportRemovalPolicyTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testConstantPolicies() {
    assertTrue(ExportRemovalPolicy.TRUE.removeExport(TestFactory.createAnArchivalChecksum()));
    assertFalse(ExportRemovalPolicy.FALSE.removeExport(TestFactory.createAnArchivalChecksum()));
  }
}
