package com.researchspace.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImportContextTest {
  private ImportContext context = null;

  @BeforeEach
  public void setUp() throws Exception {
    context = new ImportContext();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testIgnoreUnpublishedForms() {
    assertTrue(context.ignoreUnpublishedForms());
    assertTrue(context.enableDirectTemplateCreationInTemplateFolder());
  }
}
