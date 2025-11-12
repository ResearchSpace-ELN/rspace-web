package com.axiope.service.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.axiope.service.cfg.EcatMediaFactorySetupTest.EcatMediaFactorySetupTestDefault;
import com.axiope.service.cfg.EcatMediaFactorySetupTest.EcatMediaFactorySetupTestNonDefault;
import com.researchspace.service.IMediaFactory;
import com.researchspace.testutils.SpringTransactionalTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

//
// tests that configuration is passed in correctly and defaults are set.
@RunWith(Suite.class)
@SuiteClasses({EcatMediaFactorySetupTestDefault.class, EcatMediaFactorySetupTestNonDefault.class})
public class EcatMediaFactorySetupTest {

  @Nested
  public class EcatMediaFactorySetupTestDefault extends SpringTransactionalTest {
    private @Autowired IMediaFactory factory;

    @Test
    public void test() {
      // default in the active RSDev profile for running tests
      assertEquals(100000, factory.getMaxImageMemorySize());
    }
  }

  @Nested
  @TestPropertySource(properties = {"max.tiff.conversionSize=100"})
  public class EcatMediaFactorySetupTestNonDefault extends SpringTransactionalTest {
    private @Autowired IMediaFactory factory;

    @Test
    public void test() {
      assertEquals(100, factory.getMaxImageMemorySize());
    }
  }
}
