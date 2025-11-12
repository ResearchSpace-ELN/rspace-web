package com.researchspace.service;

import static org.junit.jupiter.api.Assertions.*;

import com.researchspace.model.FileStoreRoot;
import com.researchspace.testutils.SpringTransactionalTest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FileStoreRootDetectorTest extends SpringTransactionalTest {
  @Autowired
  @Qualifier("fileStoreRootDetector")
  IApplicationInitialisor initialisor;

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testOnAppStartup() {
    initialisor.onAppStartup(null);
    // should be exactly 1 current file store

    List<FileStoreRoot> rc =
        sessionFactory
            .getCurrentSession()
            .createQuery("from FileStoreRoot where current=:curr", FileStoreRoot.class)
            .setParameter("curr", true)
            .list();
    if (rc.size() == 1) {
      assertFalse(rc.get(0).isExternal());
    } else if (rc.size() == 2) {
      assertTrue(rc.stream().filter(fsr -> fsr.isExternal()).findFirst().isPresent());
      assertTrue(rc.stream().filter(fsr -> !fsr.isExternal()).findFirst().isPresent());
    } else {
      fail(" There are " + rc.size() + "file stores");
    }
  }
}
