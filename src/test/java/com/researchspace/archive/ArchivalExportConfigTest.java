package com.researchspace.archive;

import static org.junit.jupiter.api.Assertions.*;

import com.researchspace.archive.model.ArchiveExportConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchivalExportConfigTest {

  ArchiveExportConfig cfg;

  @BeforeEach
  public void setUp() throws Exception {
    cfg = new ArchiveExportConfig();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGenerateDocumentExportFileName() {
    assertTrue(cfg.isArchive()); // default
    assertEquals("file.xml", cfg.generateDocumentExportFileName("file"));
    cfg.setArchiveType(ArchiveExportConfig.HTML);
    assertEquals("file.html", cfg.generateDocumentExportFileName("file"));
  }

  @Test
  public void testIsSelection() {
    assertFalse(cfg.isSelectionScope());
    cfg.setExportScope(ExportScope.SELECTION);
    assertTrue(cfg.isSelectionScope());
  }
}
