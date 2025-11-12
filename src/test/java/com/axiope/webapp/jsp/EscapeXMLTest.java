package com.axiope.webapp.jsp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EscapeXMLTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void test() {
    assertEquals("Bob&#039;s document", EscapeXml.escape("Bob's document"));

    assertEquals(
        "&lt;script&gt;alert(1)&lt;/script&gt;", EscapeXml.escape("<script>alert(1)</script>"));
  }
}
