package com.researchspace.webapp.controller.repositories;

import static com.researchspace.service.IntegrationsHandler.DIGITAL_COMMONS_DATA_APP_NAME;

import com.researchspace.model.User;
import com.researchspace.model.oauth.UserConnection;
import com.researchspace.properties.IPropertyHolder;
import com.researchspace.service.UserConnectionManager;
import com.researchspace.webapp.integrations.dcd.DigitalCommonsDataController;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.common.security.SimplePrincipal;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class DigitalCommonsDataUIConnectionConfig implements RSpaceRepoConnectionConfig {

  private User subject;
  private UserConnectionManager userConnectionManager;
  private DigitalCommonsDataController digitalCommonsDataController;
  private IPropertyHolder propertyHolder;
  private RestTemplate restTemplate;
  private static final String FAKE_ID = "FAKE_ID";

  public DigitalCommonsDataUIConnectionConfig(
      DigitalCommonsDataController digitalCommonsDataController,
      UserConnectionManager source, User subject, IPropertyHolder propertyHolder) {
    this.subject = subject;
    this.userConnectionManager = source;
    this.propertyHolder = propertyHolder;
    this.digitalCommonsDataController = digitalCommonsDataController;
    this.restTemplate = new RestTemplate();
  }

  @Override
  public Optional<URL> getRepositoryURL() {
    try {
      return Optional.of(new URL(this.propertyHolder.getDigitalCommonsDataBaseUrl()));
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          "Couldn't create Digital Commons Data repositoryURL " + e.getMessage());
    }
  }

  @Override
  public String getApiKey() {
    Optional<UserConnection> optUserConnection =
        userConnectionManager.findByUserNameProviderName(
            subject.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME);
    Principal principal = new SimplePrincipal(subject.getUsername());
    if (optUserConnection.isEmpty()) {
      new IllegalArgumentException(
          "No UserConnection exists for: " + DIGITAL_COMMONS_DATA_APP_NAME);
    }
    String accessToken = optUserConnection.get().getAccessToken();

    if (!digitalCommonsDataController.isConnectionAlive(principal)){
      digitalCommonsDataController.refreshToken(new BindingAwareModelMap(), principal);
      accessToken = userConnectionManager.findByUserNameProviderName(
          subject.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME).get().getAccessToken();
    }
    return accessToken;
  }

//  private String refreshToken() throws HttpStatusCodeException {
//    DcdAccessToken accessToken;
//    UserConnection userConnection =
//        userConnectionManager
//            .findByUserNameProviderName(subject.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME)
//            .get();
//    if (StringUtils.isBlank(userConnection.getRefreshToken())) {
//      throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
//    }
//
//    accessToken = doRefreshToken(userConnection.getRefreshToken());
//    userConnection.setAccessToken(accessToken.getAccessToken());
//    userConnection.setRefreshToken(accessToken.getRefreshToken());
//    userConnection.setExpireTime(getExpireTime(accessToken.getExpiresIn()));
//    userConnection.setDisplayName("DigitalCommonsData refreshed access token");
//    userConnectionManager.save(userConnection);
//    log.info("Connected DigitalCommonsData for user {}", subject.getUsername());
//    return accessToken.getAccessToken();
//  }

//  protected DcdAccessToken doRefreshToken(String refreshToken) throws HttpStatusCodeException {
//
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//    headers.setBasicAuth(
//        propertyHolder.getDigitalCommonsDataClientId(),
//        propertyHolder.getDigitalCommonsDataClientSecret());
//    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//    formData.add("grant_type", "refresh_token");
//    formData.add("refresh_token", refreshToken);
//    formData.add(
//        "redirect_uri",
//        propertyHolder.getDigitalCommonsDataCallbackBaseUrl()
//            + "/apps/digitalcommonsdata/callback");
//    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
//
//    return restTemplate
//        .exchange(
//            getAuthBaseUrl() + "/oauth2/token", HttpMethod.POST, request, DcdAccessToken.class)
//        .getBody();
//  }

//  protected Boolean isConnectionAlive(String accessToken) {
//    if (StringUtils.isNotBlank(accessToken)) {
//      String expectedMsg =
//          "404 Not Found: \"{\"message\":\"Draft dataset '" + FAKE_ID + "' not found\"}\"";
//      try {
//        doCheckConnectionAlive(accessToken);
//      } catch (HttpClientErrorException clientEx) {
//        if (expectedMsg.equals(clientEx.getMessage())) {
//          return Boolean.TRUE;
//        }
//      } catch (Exception e) {
//        log.error("Couldn't perform test action {}", e.getMessage());
//        return Boolean.FALSE;
//      }
//    }
//    return Boolean.FALSE;
//  }


//  protected void doCheckConnectionAlive(String accessToken) {
//    HttpHeaders headers = new HttpHeaders();
//    headers.add("Authorization", "Bearer " + accessToken);
//    restTemplate.exchange(
//        getApiBaseUrl() + "/active-data-entities/datasets/drafts/" + FAKE_ID,
//        HttpMethod.DELETE,
//        new HttpEntity<>(headers),
//        Object.class);
//  }
//
//  private long getExpireTime(Long expiresInSeconds) {
//    return Instant.now().toEpochMilli() + (expiresInSeconds * 1000);
//  }
//
//  private String getAuthBaseUrl() {
//    return propertyHolder.getDigitalCommonsDataBaseUrl().replace("://", "://auth.");
//  }

  private String getApiBaseUrl() {
    return propertyHolder.getDigitalCommonsDataBaseUrl().replace("://", "://api.");
  }

}
