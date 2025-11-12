package com.researchspace.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.ArchiveVersionToAppVersion;
import com.researchspace.model.Version;
import com.researchspace.testutils.SpringTransactionalTest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataTest extends SpringTransactionalTest {
  @Autowired RSMetaDataDao dao;

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGetSchemaVersions() {
    List<ArchiveVersionToAppVersion> versions = dao.getArchiveVersionsToAppVersion();
    assertTrue(versions.size() > 1);
  }

  @Test
  public void testGetSchemaVersion() {
    ArchiveVersionToAppVersion version = dao.getAppVersionForArchiveVersion(new Version(1L), "doc");
    assertNotNull(version);
  }
}
