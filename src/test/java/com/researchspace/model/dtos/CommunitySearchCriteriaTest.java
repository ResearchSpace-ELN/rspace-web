package com.researchspace.model.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.researchspace.core.testutil.CoreTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommunitySearchCriteriaTest {

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {}

  @Test
  public void testGetAllFields() {
    CommunitySearchCriteria crit = new CommunitySearchCriteria();
    crit.setDisplayName(CoreTestUtils.getRandomName(300));
    assertEquals(255, crit.getDisplayName().length());
  }
}
