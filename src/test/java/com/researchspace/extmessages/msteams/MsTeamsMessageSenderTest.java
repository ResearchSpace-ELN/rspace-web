package com.researchspace.extmessages.msteams;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.apps.App;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MsTeamsMessageSenderTest {

  MsTeamsMessageSender msteamsSender;

  @BeforeEach
  public void setUp() throws Exception {
    msteamsSender = new MsTeamsMessageSender();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testSupportsApp() {
    assertTrue(msteamsSender.supportsApp(new App(App.APP_MSTEAMS, "any", false)));
    assertFalse(msteamsSender.supportsApp(new App(App.APP_SLACK, "any", false)));
  }
}
