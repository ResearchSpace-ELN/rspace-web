package com.researchspace.files.service;

import static com.researchspace.model.record.TestFactory.createAnyUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.researchspace.model.User;
import com.researchspace.model.oauth.UserConnection;
import com.researchspace.model.record.TestFactory;
import com.researchspace.service.UserConnectionManager;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ExternalFileStoreLocatorImplTest {

  @Mock UserConnectionManager userConnectionMgr;
  @Mock ExternalFileStore extFileStore;
  ExternalFileStoreLocatorImpl extFileLocator;
  User user;

  @BeforeEach
  public void setup() throws IOException {
    user = createAnyUser("user");
    extFileLocator =
        new ExternalFileStoreLocatorImpl(ExternalFileStoreProvider.EGNYTE, extFileStore);
    extFileLocator.setUserConnectionMgr(userConnectionMgr);
  }

  @Test
  public void getExternalFileStoreForUser() throws IOException {
    UserConnection userConnection = getUserConnectionForUser();
    setUpUserConnectionExists(userConnection);
    Optional<ExternalFileStoreWithCredentials> efs =
        extFileLocator.getExternalFileStoreForUser(user.getUsername());
    assertTrue(efs.isPresent());
    assertEquals(userConnection, efs.get().getUserConnection());
    assertEquals(extFileStore, efs.get().getExtFileStore());
  }

  @Test
  public void getExternalFileStoreForUserREturnsEmptyIfNoUserConnection() throws IOException {
    UserConnection uc = getUserConnectionForUser();
    setUpUserConnectionNotExists(uc);
    Optional<ExternalFileStoreWithCredentials> efs =
        extFileLocator.getExternalFileStoreForUser(user.getUsername());
    assertFalse(efs.isPresent());
  }

  private UserConnection getUserConnectionForUser() {
    return TestFactory.createUserConnection(user.getUsername());
  }

  private void setUpUserConnectionNotExists(UserConnection uc) {
    when(userConnectionMgr.findByUserNameProviderName(
            user.getUsername(), ExternalFileStoreProvider.EGNYTE.name()))
        .thenReturn(Optional.empty());
  }

  private void setUpUserConnectionExists(UserConnection uc) {
    when(userConnectionMgr.findByUserNameProviderName(
            user.getUsername(), ExternalFileStoreProvider.EGNYTE.name()))
        .thenReturn(Optional.of(uc));
  }
}
