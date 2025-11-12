package com.researchspace.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.Version;
import com.researchspace.testutils.SpringTransactionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RSMetaDataServiceTest extends SpringTransactionalTest {

  @Autowired RSMetaDataManager metadataMgr;

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testIsArchiveImportable() {
    assertTrue(metadataMgr.isArchiveImportable("doc", new Version(1L)));
    // too late
    assertFalse(metadataMgr.isArchiveImportable("doc", new Version(20L)));
    // unknown schema type
    assertFalse(metadataMgr.isArchiveImportable("docxxxx", new Version(1L)));
  }
}
