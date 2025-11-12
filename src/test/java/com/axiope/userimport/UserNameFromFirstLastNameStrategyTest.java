package com.axiope.userimport;

import com.researchspace.model.User;
import com.researchspace.model.dto.UserRegistrationInfo;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserNameFromFirstLastNameStrategyTest {

  private UserNameCreationStrategy strat = new UserNameFromFirstLastNameStrategy();

  @Test
  void testCreateUserNameFromCandidate() {
    UserRegistrationInfo u = new UserRegistrationInfo();
    Assertions.assertTrue(strat.createUserName("anything", u, new HashSet<String>()));
    Assertions.assertTrue(u.getUsername().contains("anything"));

    // check can create even if there is a duplicate
    UserRegistrationInfo u2 = new UserRegistrationInfo();
    Set<String> seen = new HashSet<String>();
    seen.add("anything");
    Assertions.assertTrue(strat.createUserName("anything", u2, seen));
  }

  @Test
  void testCreateUserNamePadsLEngthToMinLength() {
    UserRegistrationInfo userInfo = new UserRegistrationInfo();
    // too short uname
    Assertions.assertTrue(strat.createUserName("bob", userInfo, new HashSet<String>()));
    Assertions.assertTrue(userInfo.getUsername().length() >= User.MIN_UNAME_LENGTH);
  }

  @Test
  void testCreateUserNameFrom1stLAstName() {
    UserRegistrationInfo userInfo = new UserRegistrationInfo();
    userInfo.setFirstName("Bob");
    userInfo.setLastName("Jones");
    // no candidate uname, but can generate from 1st and last names
    Assertions.assertTrue(strat.createUserName("", userInfo, new HashSet<String>()));
    Assertions.assertTrue(userInfo.getUsername().contains("bjones"));

    // but can't work maginc; cannot create uname if no information supplied
    UserRegistrationInfo u2 = new UserRegistrationInfo();
    Assertions.assertFalse(strat.createUserName("", u2, new HashSet<String>()));
  }
}
