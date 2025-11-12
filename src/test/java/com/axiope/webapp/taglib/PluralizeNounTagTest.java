package com.axiope.webapp.taglib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PluralizeNounTagTest {
  PluralizeNounTag tag = new PluralizeNounTag();

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testDoStartTag() {
    tag.setCount(2);
    tag.setInput("blog");
    assertEquals("blogs", tag.getString());

    tag.setCount(1);

    assertEquals("blog", tag.getString());
  }
}
