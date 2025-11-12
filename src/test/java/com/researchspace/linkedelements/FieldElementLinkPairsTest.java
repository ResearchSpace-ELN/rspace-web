package com.researchspace.linkedelements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.EcatImage;
import com.researchspace.model.EcatMediaFile;
import com.researchspace.model.IFieldLinkableElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FieldElementLinkPairsTest {
  private FieldElementLinkPairs<EcatImage> images = new FieldElementLinkPairs<>(EcatImage.class);
  private FieldElementLinkPairs<IFieldLinkableElement> all =
      new FieldElementLinkPairs<>(IFieldLinkableElement.class);

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testSupportsClass() {
    assertTrue(images.supportsClass(EcatImage.class));
    assertFalse(images.supportsClass(EcatMediaFile.class));
    assertFalse(images.supportsClass(IFieldLinkableElement.class));

    assertTrue(all.supportsClass(EcatImage.class));
    assertTrue(all.supportsClass(EcatMediaFile.class));
    assertTrue(all.supportsClass(IFieldLinkableElement.class));
  }
}
