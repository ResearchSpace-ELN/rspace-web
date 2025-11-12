package com.researchspace.archive.model;

import static org.junit.jupiter.api.Assertions.*;

import com.researchspace.archive.ArchiveFolder;
import com.researchspace.model.User;
import com.researchspace.model.core.RecordType;
import com.researchspace.model.record.Folder;
import com.researchspace.model.record.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveFolderTest {

  ArchiveModelFactory fac;

  @BeforeEach
  public void setUp() throws Exception {
    fac = new ArchiveModelFactory();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testArchiveFolderHandlesNullPArentOK() {
    User any = TestFactory.createAnyUser("any");
    Folder child = TestFactory.createAFolder("f", any);
    child.setId(1L);
    ArchiveFolder af = fac.createArchiveFolder(child);
    assertNull(af.getParentId());
    Folder parent = TestFactory.createAFolder("f2", any);
    parent.addChild(child, any);
    parent.setId(2L);
    any.setRootFolder(parent);
    ArchiveFolder af2 = fac.createArchiveFolder(child);
    assertEquals(2L, af2.getParentId().longValue());
  }

  @Test
  public void testIsSystemType() {
    User any = TestFactory.createAnyUser("any");
    Folder child = TestFactory.createAFolder("f", any);
    child.setId(1L);
    ArchiveFolder af = fac.createArchiveFolder(child);
    assertFalse(af.isSystemFolder());
    child.addType(RecordType.SYSTEM);
    af = fac.createArchiveFolder(child);
    assertTrue(af.isSystemFolder());
    child.setType(null);
    af = fac.createArchiveFolder(child);
    assertFalse(af.isSystemFolder());
  }
}
