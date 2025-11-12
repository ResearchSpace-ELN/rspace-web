package com.researchspace.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.researchspace.core.util.TransformerUtils;
import com.researchspace.model.User;
import com.researchspace.model.record.TestFactory;
import com.researchspace.service.UserExistsException;
import com.researchspace.service.UserManager;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectRetrievalFailureException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ExternalOauthUserSignupPolicyTest {

  @InjectMocks ExternalOauthUserSignupPolicy policy;
  @Mock UserManager userMgr;
  MockHttpServletRequest req;
  ;

  @BeforeEach
  public void setUp() throws Exception {
    req = new MockHttpServletRequest();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testSaveNewUserHappyCase() throws UserExistsException {
    User user = TestFactory.createAnyUser("user1");
    policy.saveUser(user, req);
    Mockito.verify(userMgr, Mockito.times(1)).saveNewUser(user);
  }

  @Test
  public void testSaveNewUserExistingEmailDoesntLoging() throws UserExistsException {
    User user = TestFactory.createAnyUser("user1");
    User fromDBUserWithSameEmail = TestFactory.createAnyUser("user2");
    fromDBUserWithSameEmail.setEmail(user.getEmail());
    List<User> fromDB = TransformerUtils.toList(fromDBUserWithSameEmail);
    when(userMgr.saveNewUser(user)).thenThrow(userExistsException());
    when(userMgr.getUserByEmail(user.getEmail())).thenReturn(fromDB);
    assertEquals(fromDBUserWithSameEmail, policy.saveUser(user, req));
    // login is done elsewhere, we don't need to login here

  }

  @Test
  public void testSaveNewUserExistingUsername() {
    assertThrows(
        UserExistsException.class,
        () -> {
          User user = TestFactory.createAnyUser("user1");
          User fromDBUserWithSameUsername = TestFactory.createAnyUser("user1");
          fromDBUserWithSameUsername.setEmail("differeentEmail@test.com");

          List<User> fromDB = Collections.emptyList();
          when(userMgr.saveNewUser(user)).thenThrow(userExistsException());
          when(userMgr.getUserByEmail(user.getEmail())).thenReturn(fromDB);
          policy.saveUser(user, req);
        });
  }

  @Test
  public void testSaveNewUserISEForUnlikelyScenario() throws UserExistsException {
    assertThrows(
        IllegalStateException.class,
        () -> {
          // diff usernames and email but not signed up
          User user = TestFactory.createAnyUser("user1");
          User other = TestFactory.createAnyUser("user2");
          other.setEmail("differeentEmail@test.com");

          List<User> fromDB = Collections.emptyList();
          when(userMgr.saveNewUser(user)).thenThrow(userExistsException());
          when(userMgr.getUserByEmail(user.getEmail())).thenReturn(fromDB);
          when(userMgr.getUserByUsername(user.getUsername())).thenThrow(objectRetrievalEXc());
          policy.saveUser(user, req);
        });
  }

  private ObjectRetrievalFailureException objectRetrievalEXc() {
    return new ObjectRetrievalFailureException("no user", new RuntimeException());
  }

  private UserExistsException userExistsException() {
    return new UserExistsException("User exists");
  }
}
