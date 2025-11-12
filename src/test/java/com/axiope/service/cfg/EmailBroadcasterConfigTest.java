package com.axiope.service.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.researchspace.service.impl.EmailBroadcastImp;
import com.researchspace.service.impl.StrictEmailContentGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

/**
 * This test verifies how different deployment property settings affect the configuration of
 * EmailBroadcaster.
 */
@SpringJUnitConfig(classes = {EmailBroadcasterConfigTest.EmailConfig.class})
@ActiveProfiles(profiles = {"emailConfig"})
@TestPropertySource(properties = {"mail.maxEmailsPerSecond=23", "mail.addressChunkSize=21"})
public class EmailBroadcasterConfigTest {

  @Configuration
  @Profile(
      "emailConfig") // needs to define its own profile so that it doesn't pollute configuration of
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

  private @Autowired EmailBroadcastImp emailImpl;

  @Test
  public void testEmbeddedConfig() {
    assertEquals(23, emailImpl.getMaxSendingRate().intValue());
  }
}
