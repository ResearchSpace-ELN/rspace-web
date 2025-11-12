package com.axiope.service.cfg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.service.RepositoryFactory;
import com.researchspace.testutils.SpringTransactionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryFactoryTest extends SpringTransactionalTest {

  @Autowired private RepositoryFactory repoFactory;

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testIsPrototype() {
    assertTrue(repoFactory.getRepository() != null);
    assertFalse(repoFactory.getRepository() == repoFactory.getRepository());
  }
}
