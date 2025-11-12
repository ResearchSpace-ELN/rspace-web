package com.researchspace.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupUsageInfoTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGetPercent() {
    GroupUsageInfo info = new GroupUsageInfo(null, 1L, 10L);
    assertEquals(10d, info.getPercent(), 0.001);

    info = new GroupUsageInfo(null, 1L, 0L);
    assertEquals(-1, info.getPercent(), 0.001);

    info = new GroupUsageInfo(null, 1L, 1L);
    assertEquals(100, info.getPercent(), 0.001);
  }
}
