package com.researchspace.webapp.integrations.dmponline;

import static com.researchspace.service.IntegrationsHandler.DMPTOOL_APP_NAME;

import com.researchspace.analytics.service.AnalyticsManager;
import com.researchspace.dmptool.model.DMPPlanScope;
import com.researchspace.model.User;
import com.researchspace.model.dmps.DMPUser;
import com.researchspace.model.oauth.UserConnection;
import com.researchspace.model.views.ServiceOperationResult;
import com.researchspace.rda.model.DMP;
import com.researchspace.rda.model.DMPList;
import com.researchspace.service.UserConnectionManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/** Rest client for DMPTool API */
@Slf4j
public class DMPOnlnineProviderImpl implements DMPOnlineProvider {

  private @Autowired UserConnectionManager userConnectionManager;
  private @Autowired AnalyticsManager analyticsManager;

  private final RestTemplate restTemplate;

  public DMPOnlnineProviderImpl(URL baseUrl) {
    this.restTemplate = new RestTemplate();
  }

  protected String getJson(DMP dmp, String accessToken)
      throws URISyntaxException, MalformedURLException {
    // TODO[nik]: implement this and call it from the controller

    String json = ""; // this.dmpToolClient.getJson(dmp, accessToken);
    return json;
  }

  private DMPList listPlans(DMPPlanScope scope, String accessToken)
      throws MalformedURLException, URISyntaxException {
    // TODO[nik]: implement this and call it from the controller

    return null; // this.dmpToolClient.listPlans(scope, accessToken);
  }

  @Override
  public ServiceOperationResult<DMPList> listPlans(DMPPlanScope scope, User user)
      throws MalformedURLException, URISyntaxException {
    // TODO[nik]: implement this and call it from the controller

    Optional<UserConnection> optConn = getUserConnection(user.getUsername());
    if (!optConn.isPresent()) {
      return new ServiceOperationResult<>(null, false, noAccessTokenMsg());
    }
    var apiDMPlanList = listPlans(scope, optConn.get().getAccessToken());
    analyticsManager.dmpsViewed(user);
    return new ServiceOperationResult<>(apiDMPlanList, true);
  }

  @Override
  public ServiceOperationResult<DMP> getPlanById(String dmpId, User user)
      throws MalformedURLException, URISyntaxException {
    // TODO[nik]: implement this and call it from the controller
    return new ServiceOperationResult<>(null, true);
  }

  private DMP getPlanById(String dmpId, String accessToken)
      throws MalformedURLException, URISyntaxException {
    // TODO[nik]: implement this and call it from the controller
    return null; // this.dmpToolClient.getPlanById(dmpId, accessToken);
  }

  private String noAccessTokenMsg() {
    return "Access token isn't enabled - user must connect in Apps page";
  }

  private DMPUser doJsonDownload(DMP dmp, String title, String accessToken)
      throws URISyntaxException, IOException {
    // TODO[nik]: implement this and call it from the controller

    return null; // saveJsonDMP(dmp, title, user, getJson(dmp, accessToken));
  }

  @Override
  public ServiceOperationResult<DMPUser> doJsonDownload(DMP dmp, String title, User user)
      throws URISyntaxException, IOException {
    // TODO[nik]: implement this and call it from the controller

    Optional<UserConnection> optConn = getUserConnection(user.getUsername());
    if (!optConn.isPresent()) {
      return noAccessTokenFailure(DMPUser.class);
    } else {
      DMPUser created = doJsonDownload(dmp, title, optConn.get().getAccessToken());
      if (created != null) {
        return new ServiceOperationResult<>(created, true);
      } else {
        return new ServiceOperationResult<>(
            null,
            false,
            "A DMP with id " + dmp.getId() + ", title: " + dmp.getTitle() + " already exists");
      }
    }
  }

  private <T> ServiceOperationResult<T> noAccessTokenFailure(Class<T> clazz) {
    return new ServiceOperationResult<>(null, false, noAccessTokenMsg());
  }

  private HttpHeaders makeApiHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("Authorization", String.format("Bearer %s", accessToken));
    return headers;
  }

  private Optional<UserConnection> getUserConnection(String username) {
    Optional<UserConnection> optConn =
        userConnectionManager.findByUserNameProviderName(username, DMPTOOL_APP_NAME);
    if (!optConn.isPresent()) {
      log.error("No DMPtool OAuth connection found for user {}", username);
    }
    return optConn;
  }
}
