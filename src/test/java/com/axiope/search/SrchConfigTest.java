package com.axiope.search;

import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SrchConfigTest {

  SearchConfig srchCfg;

  @BeforeEach
  void setUp() {
    srchCfg = new WorkspaceSearchConfig();
  }

  @Test
  void testGetUserFilterList() {
    Assertions.assertNotNull(srchCfg.getUsernameFilter());
    srchCfg.setUsernameFilter(Collections.emptyList());
    Assertions.assertNotNull(srchCfg.getUsernameFilter());
  }
}
