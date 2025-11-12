package com.researchspace.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultContextTest {

  private DefaultRecordContext context = null;

  @BeforeEach
  public void setUp() throws Exception {
    context = new DefaultRecordContext();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testIgnoreUnpublishedForms() {
    assertFalse(context.ignoreUnpublishedForms());
    assertFalse(context.enableDirectTemplateCreationInTemplateFolder());
  }
}
