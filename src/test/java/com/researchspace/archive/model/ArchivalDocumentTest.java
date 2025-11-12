package com.researchspace.archive.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.archive.ArchivalDocument;
import com.researchspace.model.record.StructuredDocument;
import com.researchspace.model.record.TestFactory;
import com.researchspace.testutils.ArchiveTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchivalDocumentTest {
  ArchiveModelFactory fac;

  @BeforeEach
  public void setUp() throws Exception {
    fac = new ArchiveModelFactory();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testArchivalDocumentToFromXMLRoundTrip() throws Exception {
    StructuredDocument sd = TestFactory.createWiredFolderAndDocument();
    ArchivalDocument original = fac.createArchivalDocument(sd);
    ArchivalDocument fromXML =
        ArchiveTestUtils.writeToXMLAndReadFromXML(original, ArchivalDocument.class);
    assertTrue(
        ArchiveTestUtils.areEquals(original, fromXML),
        "Original and from XML have different properties");
  }
}
