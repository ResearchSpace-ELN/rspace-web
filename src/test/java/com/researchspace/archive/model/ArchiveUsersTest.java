package com.researchspace.archive.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.Community;
import com.researchspace.model.User;
import com.researchspace.model.UserProfile;
import com.researchspace.testutils.ArchiveTestUtils;
import java.util.Iterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveUsersTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testArchiveUsersRoundTrip() throws Exception {

    ArchiveUsersTestData testData = ArchiveTestUtils.createArchiveUsersTestData();

    ArchiveUsers fromXml = ArchiveTestUtils.writeToXMLAndReadFromXML(testData.getArchiveInfo());
    Iterator<User> it = fromXml.getUsers().iterator();
    assertEquals(2, fromXml.getUserGroups().size());
    assertEquals(1, fromXml.getGroups().size());
    assertEquals(1, fromXml.getCommunities().size());
    User userRead = it.next();
    assertEquals(testData.getUser(), userRead);
    assertTrue(userRead.isInSameGroupAs(testData.getAdmin()));
    assertEquals(testData.getAdmin(), it.next());
    assertTrue(fromXml.getUserPreferences().iterator().next().getValueAsBoolean());
    // check communities
    Community inCommunity = fromXml.getCommunities().iterator().next();
    assertEquals(testData.getGroup(), inCommunity.getLabGroups().iterator().next());
    assertEquals(testData.getAdmin(), inCommunity.getAdmins().iterator().next());
    // profiles
    UserProfile profileIn = fromXml.getProfiles().iterator().next();
    assertEquals("blah", profileIn.getProfileText());
    assertTrue(true);
  }
}
