package com.researchspace.webapp.integrations.digitalcommonsdata;


import static com.researchspace.service.IntegrationsHandler.DIGITAL_COMMONS_DATA_APP_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.researchspace.model.User;
import com.researchspace.model.oauth.UserConnection;
import com.researchspace.model.oauth.UserConnectionId;
import com.researchspace.service.UserConnectionManager;
import com.researchspace.testutils.SpringTransactionalTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DigitalCommonsDataControllerTest extends SpringTransactionalTest {

  @Autowired
  private DigitalCommonsDataController digitalCommonsDataController;

  @Autowired
  private UserConnectionManager userConnectionManager;

  private User testUser;
  private UserConnectionId userConnectionId;

  @Before
  public void setUp() {
    testUser = createAndSaveUserIfNotExists("testUser");
    initialiseContentWithEmptyContent(testUser);
    assertTrue(testUser.isContentInitialized());
    userConnectionId =
        new UserConnectionId(
            testUser.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME, "ProviderUserIdNotNeeded");

    UserConnection userConnection = new UserConnection();
    userConnection.setId(userConnectionId);
    userConnection.setAccessToken("ACCESS_TOKEN");
    userConnection.setRefreshToken("REFRESH_TOKEN");
    userConnection.setExpireTime(299L);
    userConnection.setDisplayName("DigitalCommonsData access token");
    userConnectionManager.save(userConnection);
  }

  @After
  public void tearDownConnection() {
    userConnectionManager.deleteByUserAndProvider(
        DIGITAL_COMMONS_DATA_APP_NAME, testUser.getUsername());
  }

  @Test
  public void testGetUserConnectionSucceed() {
    UserConnection actualConnection =
        digitalCommonsDataController.getUserConnection(testUser.getUsername()).orElseGet(null);

    assertNotNull(actualConnection);
    assertEquals("ACCESS_TOKEN", actualConnection.getAccessToken());
    assertEquals("REFRESH_TOKEN", actualConnection.getRefreshToken());
    assertEquals("299", actualConnection.getExpireTime().toString());
    assertEquals("DigitalCommonsData access token", actualConnection.getDisplayName());
  }

  @Test
  public void testGetUserConnectionFails() {
    UserConnection actualConnection =
        digitalCommonsDataController.getUserConnection("wrong_username").orElse(null);
    assertNull(actualConnection);
  }


}