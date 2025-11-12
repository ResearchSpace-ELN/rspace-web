package com.researchspace.webapp.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Only for local test of Remote user login. Other test needs to change the proper url. Ignore as
 * default, due to url change required
 */
@Disabled
public class HttpClientTest {
  private static String url = "http://localhost:8080/";

  /**
   * This test for case: a Rspace user pre-registered in Rspace, and using SSO login. i.e the user
   * will be for both SSO or regular user: as discussed the case will not implemented
   */
  @Test
  public void testGetHttpRequestHeaderForExistingUser() {
    boolean fg = false;
    HttpClient client = new HttpClient();

    GetMethod method = new GetMethod(url);
    method
        .getParams()
        .setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
    method.setRequestHeader(new Header(EASERemoteUserPolicy.RMU_HEADER, "sunny"));
    try {
      int statusCode = client.executeMethod(method);
      if (statusCode != HttpStatus.SC_OK)
        System.err.println("Method failed: " + method.getStatusLine());

      byte[] responseBody = method.getResponseBody();

      fg = true;
    } catch (HttpException e) {

      e.printStackTrace();
    } catch (IOException e) {

      e.printStackTrace();
    } finally {
      method.releaseConnection();
    }

    assertTrue(fg);
  }
}
