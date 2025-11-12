package com.axiope.userimport;

import com.researchspace.model.dto.UserRegistrationInfo;
import com.researchspace.properties.IMutablePropertyHolder;
import com.researchspace.properties.PropertyHolder;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

class UserLineParserFromCSVTest {

  private UserLineParserFromCSV lineParser;
  private IMutablePropertyHolder properties;

  private String USER_LINE_VALID_STANDALONE =
      "Fred, Blogs, fbloggs@gmail.com, ROLE_USER, user1, testPass";
  private String USER_LINE_VALID_CLOUD =
      "Fred, Blogs, fbloggs@gmail.com, Univeristy of Qwerty, ROLE_USER, user1, testPass";
  private String USER_LINE_VALID_SSO = "Fred, Blogs, fbloggs@gmail.com, ROLE_USER, user1";

  @BeforeEach
  public void setUp() throws Exception {
    lineParser = new UserLineParserFromCSV();
    lineParser.setUnamecreationStrategy(new UserNameFromFirstLastNameStrategy());

    ResourceBundleMessageSource msgSource = new ResourceBundleMessageSource();
    msgSource.setUseCodeAsDefaultMessage(true);
    lineParser.setMessages(msgSource);

    properties = new PropertyHolder();
    lineParser.setProperties(properties);
  }

  @Test
  void testImportStandaloneUser() throws IOException {

    properties.setStandalone("true");
    properties.setCloud("false");

    Set<String> seenUsernames = new HashSet<>();
    String result = lineParser.populateUserInfo(null, USER_LINE_VALID_CLOUD, 1, seenUsernames);
    Assertions.assertEquals("system.csvimport.user.wrongNumberOfFields", result);
    String result2 = lineParser.populateUserInfo(null, USER_LINE_VALID_SSO, 1, seenUsernames);
    Assertions.assertEquals("system.csvimport.user.wrongNumberOfFields", result2);

    UserRegistrationInfo tempUser = new UserRegistrationInfo();
    String result3 =
        lineParser.populateUserInfo(tempUser, USER_LINE_VALID_STANDALONE, 1, seenUsernames);
    Assertions.assertNull(result3);

    Assertions.assertNotNull(tempUser.getUsername());
    Assertions.assertNotNull(tempUser.getPassword());
    Assertions.assertNull(tempUser.getAffiliation());
  }

  @Test
  void testImportSSOUser() throws IOException {

    properties.setStandalone("false");
    properties.setCloud("false");

    Set<String> seenUsernames = new HashSet<>();
    String result = lineParser.populateUserInfo(null, USER_LINE_VALID_STANDALONE, 1, seenUsernames);
    Assertions.assertEquals("system.csvimport.user.wrongNumberOfFields", result);
    String result2 = lineParser.populateUserInfo(null, USER_LINE_VALID_CLOUD, 1, seenUsernames);
    Assertions.assertEquals("system.csvimport.user.wrongNumberOfFields", result2);

    UserRegistrationInfo tempUser = new UserRegistrationInfo();
    String result3 = lineParser.populateUserInfo(tempUser, USER_LINE_VALID_SSO, 1, seenUsernames);
    Assertions.assertNull(result3);

    Assertions.assertNotNull(tempUser.getUsername());
    Assertions.assertNull(tempUser.getPassword());
    Assertions.assertNull(tempUser.getAffiliation());
  }

  @Test
  void testImportCloudUser() throws IOException {

    properties.setStandalone("true");
    properties.setCloud("true");

    Set<String> seenUsernames = new HashSet<>();
    String result = lineParser.populateUserInfo(null, USER_LINE_VALID_STANDALONE, 1, seenUsernames);
    Assertions.assertEquals("system.csvimport.user.wrongNumberOfFields", result);
    String result2 = lineParser.populateUserInfo(null, USER_LINE_VALID_SSO, 1, seenUsernames);
    Assertions.assertEquals("system.csvimport.user.wrongNumberOfFields", result2);

    UserRegistrationInfo tempUser = new UserRegistrationInfo();
    String result3 = lineParser.populateUserInfo(tempUser, USER_LINE_VALID_CLOUD, 1, seenUsernames);
    Assertions.assertNull(result3);

    Assertions.assertNotNull(tempUser.getUsername());
    Assertions.assertNotNull(tempUser.getPassword());
    Assertions.assertNotNull(tempUser.getAffiliation());
  }
}
