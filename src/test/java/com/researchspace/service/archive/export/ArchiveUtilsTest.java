package com.researchspace.service.archive.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.researchspace.archive.ArchiveUtils;
import com.researchspace.model.core.IRSpaceDoc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ArchiveUtilsTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGetGlobalId() {

    IRSpaceDoc doc = Mockito.mock(IRSpaceDoc.class);
    when(doc.getGlobalIdentifier()).thenReturn("SD1234");

    final String EXPECTED_URL = "http:/a.b.c.com/globalId/SD1234";
    assertEquals(
        EXPECTED_URL,
        ArchiveUtils.getAbsoluteGlobalLink(doc.getGlobalIdentifier(), "http:/a.b.c.com"));
    assertEquals(
        EXPECTED_URL,
        ArchiveUtils.getAbsoluteGlobalLink(doc.getGlobalIdentifier(), "http:/a.b.c.com/"));
  }
}
