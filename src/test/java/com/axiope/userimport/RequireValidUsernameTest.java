package com.axiope.userimport;

import com.researchspace.model.dto.UserRegistrationInfo;
import com.researchspace.model.dtos.UserValidator;
import com.researchspace.properties.IMutablePropertyHolder;
import com.researchspace.properties.PropertyHolder;
import com.researchspace.service.MessageSourceUtils;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

class RequireValidUsernameTest {

  private static final String VALID_UNAME = "abcdefg";
  private static final String SHORT_UNAME = "abcd";

  private RequireValidUserNameStrategy validUNameOnly;
  private UserValidator userValidator;
  private IMutablePropertyHolder properties;

  @BeforeEach
  void setUp() {
    validUNameOnly = new RequireValidUserNameStrategy();
    userValidator = new UserValidator();
    validUNameOnly.setValidator(userValidator);

    ResourceBundleMessageSource msgSource = new ResourceBundleMessageSource();
    msgSource.setUseCodeAsDefaultMessage(true);
    userValidator.setMessages(new MessageSourceUtils(msgSource));

    properties = new PropertyHolder();
    properties.setStandalone("true");
    properties.setMinUsernameLength(6);
    userValidator.setProperties(properties);
    userValidator.init();
  }

  @Test
  void testCreateUserName() {
    UserRegistrationInfo userRegInfo = new UserRegistrationInfo();

    Assertions.assertTrue(
        validUNameOnly.createUserName(VALID_UNAME, userRegInfo, new HashSet<String>()));
    Assertions.assertEquals(VALID_UNAME, userRegInfo.getUsername());

    userRegInfo = new UserRegistrationInfo();
    Assertions.assertFalse(
        validUNameOnly.createUserName(SHORT_UNAME, userRegInfo, new HashSet<String>()));
    // permissive validation OK
    properties.setStandalone("false");
    Assertions.assertTrue(
        validUNameOnly.createUserName(SHORT_UNAME, userRegInfo, new HashSet<String>()));
  }
}
