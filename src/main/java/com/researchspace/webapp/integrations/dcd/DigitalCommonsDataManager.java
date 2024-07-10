package com.researchspace.webapp.integrations.dcd;

import com.researchspace.model.oauth.UserConnection;
import java.util.Optional;

/** API Client wrapper for making calls to DMP API */
public interface DigitalCommonsDataManager {

  Optional<UserConnection> getUserConnection(String username);
}
