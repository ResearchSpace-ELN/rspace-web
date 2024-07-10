package com.researchspace.webapp.integrations.dcd;

import static com.researchspace.service.IntegrationsHandler.DIGITAL_COMMONS_DATA_APP_NAME;

import com.researchspace.model.oauth.UserConnection;
import com.researchspace.service.UserConnectionManager;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/** API Client wrapper for making calls to DCD API */
public class DigitalCommonsDataManagerImpl implements DigitalCommonsDataManager {

  private @Autowired UserConnectionManager userConnectionManager;

  public Optional<UserConnection> getUserConnection(String username) {
    Optional<UserConnection> optConn =
        userConnectionManager.findByUserNameProviderName(username, DIGITAL_COMMONS_DATA_APP_NAME);
    if (!optConn.isPresent()) {
      log.error("No Digital Commons Data connection found for user {}", username);
    }
    return optConn;
  }
}
