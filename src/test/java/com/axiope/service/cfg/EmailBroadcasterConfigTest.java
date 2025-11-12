package com.axiope.service.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.researchspace.service.impl.EmailBroadcastImp;
import com.researchspace.service.impl.StrictEmailContentGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

/**
 * This test class tests how different deployment property settings affect the configuration of
 * EmailBroadcaster.
 *
 * <p>By using static inner classes we can test different class-level configurations without having
 * to define every test in a separate class.
 *
 * <p>
 */
@org.junit.platform.suite.api.Suite
@SelectClasses({EmailBroadcasterConfigTest.TestBase.class})
public class EmailBroadcasterConfigTest {

  @Configuration
  @Profile(
      "emailConfig") // needs to  define its own profile so that it doesn't pollute configuration of
  // other tests
  public static class EmailConfig {
    private @Autowired Environment env;

    public EmailConfig() {}

    @Bean
    EmailBroadcastImp emailBroadcastImp() {
      Integer millis = env.getProperty("mail.maxEmailsPerSecond", Integer.class);
      Integer addressChunkSize = env.getProperty("mail.addressChunkSize", Integer.class);
      EmailBroadcastImp rc = new EmailBroadcastImp(millis, addressChunkSize);
      return rc;
    }

    @Bean(name = "velocityEngine")
    public VelocityEngineFactoryBean velocityFactoryBean() {
      return new VelocityEngineFactoryBean();
    }

    @Bean
    public StrictEmailContentGenerator strictEmailContentGenerator() {
      return new StrictEmailContentGenerator();
    }
  }

  /*
   * Base test class for common class-level configuration.
   * We  use 1 config class, so we don't have to wire up the whole application
   * to test the logic in the configuration class.
   */
  @ContextConfiguration(classes = {EmailConfig.class})
  @Nested
  @ActiveProfiles(profiles = {"emailConfig"})
  @TestPropertySource(properties = {"mail.maxEmailsPerSecond=23", "mail.addressChunkSize=21"})
  public class TestBase extends AbstractJUnit4SpringContextTests {
    private @Autowired EmailBroadcastImp emailImpl;

    @Test
    public void testEmbeddedConfig() {
      assertEquals(23, emailImpl.getMaxSendingRate().intValue());
    }
  }
}
