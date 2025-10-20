package com.researchspace.webapp.integrations.raid;

import static com.researchspace.service.IntegrationsHandler.RAID_APP_NAME;

import com.researchspace.service.raid.RaIDServiceClientAdapter;
import com.researchspace.webapp.integrations.helper.BaseOAuth2Controller;
import com.researchspace.webapp.integrations.helper.OauthAuthorizationError;
import com.researchspace.webapp.integrations.helper.OauthAuthorizationError.OauthAuthorizationErrorBuilder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/apps/raid")
public class RaIDAuthController extends BaseOAuth2Controller {

  @Autowired private RaIDServiceClientAdapter raidServiceClientAdapter;

  public RaIDAuthController() {}

  @GetMapping("/connect/{serverAlias}") // TODO[nik]: make it POST
  public RedirectView connect(@PathVariable String serverAlias)
      throws MalformedURLException, URISyntaxException {
    return new RedirectView(raidServiceClientAdapter.performRedirectConnect(serverAlias));
  }

  @GetMapping("/callback")
  public String callback(@RequestParam Map<String, String> params, Model model, Principal principal)
      throws IOException, URISyntaxException, HttpClientErrorException {
    String redirectResult;
    OauthAuthorizationErrorBuilder error = OauthAuthorizationError.builder().appName("RaID");
    try {
      String serverAlias = params.get("state");
      raidServiceClientAdapter.performCreateAccessToken(
          principal.getName(), serverAlias, params.get("code"));

      log.info("Connected RaID for user {}", principal.getName());
      redirectResult = "connect/raid/connected";
    } catch (Exception ex) {
      log.error("Couldn't complete the token request on RaID", ex);
      error.errorMsg("Error during token creation");
      error.errorMsg(ex.getMessage());
      model.addAttribute("error", error.build());
      redirectResult = "connect/authorizationError";
    }
    return redirectResult;
  }

  @GetMapping("/disconnect/{serverAlias}") // TODO[nik]: make it DELETE /connect/{serverAlias}
  public void disconnect(@PathVariable String serverAlias, Principal principal) {
    int deletedConnCount =
        userConnectionManager.deleteByUserAndProvider(
            RAID_APP_NAME, principal.getName(), serverAlias);
    log.info("Deleted {} RaID connection(s) for user {}", deletedConnCount, principal.getName());
  }

  @GetMapping("/test_connection/{serverAlias}") // TODO[nik]: make it POST
  public Boolean isConnectionAlive(@PathVariable String serverAlias, Principal principal) {
    Boolean isConnectionAlive = Boolean.TRUE;
    try {
      raidServiceClientAdapter.isRaidConnectionAlive(principal.getName(), serverAlias);
    } catch (Exception e) {
      log.error(
          "Couldn't perform test connection action on RaID. "
              + "The connection will be flagged as NOT ALIVE",
          e);
      isConnectionAlive = Boolean.FALSE;
    }
    return isConnectionAlive;
  }

  @GetMapping("/refresh_token/{serverAlias}") // TODO[nik]: make it POST
  public String refreshToken(@PathVariable String serverAlias, Model model, Principal principal) {
    OauthAuthorizationErrorBuilder error = OauthAuthorizationError.builder().appName("RaID");
    String redirectResult = "";
    try {
      raidServiceClientAdapter.performRefreshToken(principal.getName(), serverAlias);
      redirectResult = "connect/raid/connected";
    } catch (Exception e) {
      log.error("Error while refreshing token on RaID: {}", e.getMessage());
      error.errorMsg("Error during token refresh");
      error.errorDetails(e.getMessage());
      model.addAttribute("error", error.build());
      redirectResult = "connect/authorizationError";
    }

    return redirectResult;
  }
}
