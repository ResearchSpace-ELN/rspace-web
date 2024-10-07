package com.researchspace.webapp.filter;

import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/** Translates OpenId claims form httpRequest headers into user information */
@Slf4j
public class OpenIdRemoteUserPolicy extends AbstractSsoRemoteUserPolicy {

  @Value("${deployment.sso.openid.usernameClaim}")
  private String usernameClaim;

  @Value("${deployment.sso.openid.additionalUsernameClaims}")
  private String additionalUsernameClaims;

  @Value("${deployment.sso.openid.emailClaim}")
  private String emailClaim;

  @Value("${deployment.sso.openid.firstNameClaim}")
  private String firstNameClaim;

  @Value("${deployment.sso.openid.lastNameClaim}")
  private String lastNameClaim;

  @Override
  public String getRemoteUser(HttpServletRequest httpRequest) {
    log.debug("Req headers: {}", logHeaders(httpRequest));
    String username = getOpenIdUsernameFromRequestClaims(httpRequest);
    log.info("Logging in remote user: {}", username);
    return username;
  }

  private String getOpenIdUsernameFromRequestClaims(HttpServletRequest httpRequest) {
    log.debug("using usernameClaim: {}", usernameClaim);
    return httpRequest.getHeader(usernameClaim);
  }

  @Override
  public Map<RemoteUserAttribute, String> getOtherRemoteAttributes(HttpServletRequest httpRequest) {
    Map<RemoteUserAttribute, String> rc = new TreeMap<>();

    String mail = (String) httpRequest.getAttribute(emailClaim);
    if (!StringUtils.isBlank(mail)) {
      rc.put(RemoteUserAttribute.EMAIL, mail);
    }

    String firstName = httpRequest.getHeader(firstNameClaim);
    if (!StringUtils.isBlank(firstName)) {
      rc.put(RemoteUserAttribute.FIRST_NAME, firstName);
    }

    String lastName = (String) httpRequest.getAttribute(lastNameClaim);
    if (!StringUtils.isBlank(lastName)) {
      rc.put(RemoteUserAttribute.LAST_NAME, lastName);
    }

    return rc;
  }

}
