package com.researchspace.webapp.controller;

import static com.researchspace.session.SessionAttributeUtils.TIMEZONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.researchspace.auth.TimezoneAdjusterImpl;
import com.researchspace.properties.IPropertyHolder;
import com.researchspace.session.SessionAttributeUtils;
import java.io.IOException;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class TimezoneInterceptorTest {

  @Mock IPropertyHolder properties;
  @InjectMocks TimezoneInterceptor tzInterceptor;

  MockHttpServletRequest req;
  MockHttpSession session;
  TimezoneAdjusterImpl tzAdjuster;

  @BeforeEach
  public void setUp() throws Exception {
    req = new MockHttpServletRequest();
    session = new MockHttpSession();
    req.setSession(session);
    tzAdjuster = new TimezoneAdjusterImpl();
    tzInterceptor.setTimezoneAdjuster(tzAdjuster);
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testRequiresSSO() throws IOException {
    when(properties.isSSO()).thenReturn(false);
    invokeHandler();
    verifyTzSetInSession(false);
  }

  @Test
  public void testRequiresCookieAndSSO() throws IOException {
    when(properties.isSSO()).thenReturn(true);
    req.setCookies(new Cookie(TIMEZONE, "IST"));
    invokeHandler();
    verifyTzSetInSession(true);
    assertTrue((Boolean) session.getAttribute(SessionAttributeUtils.FIRST_REQUEST));
    // subsequent requests have 'first request' false
    invokeHandler();
    verifyTzSetInSession(true);
    assertFalse((Boolean) session.getAttribute(SessionAttributeUtils.FIRST_REQUEST));
  }

  @Test
  public void testRequiresNotNullCookieAndSSO() throws IOException {
    when(properties.isSSO()).thenReturn(true);
    req.setCookies(null);
    invokeHandler();
    verifyTzSetInSession(false);
  }

  @Test
  public void testRequiresCookieAndSSOAndUnsetSession() throws IOException {
    when(properties.isSSO()).thenReturn(true);
    req.setCookies(new Cookie(TIMEZONE, "IST"));
    session.setAttribute(TIMEZONE, "GMT");
    invokeHandler();
    // not updated if already set in session
    assertEquals("GMT", session.getAttribute(SessionAttributeUtils.TIMEZONE));
  }

  private void invokeHandler() throws IOException {
    tzInterceptor.preHandle(req, null, null);
  }

  @Test
  public void testRequiresValidCookieAndSSO() throws IOException {
    when(properties.isSSO()).thenReturn(true);
    // needs valid cookie value
    req.setCookies(new Cookie(TIMEZONE, ""));
    invokeHandler();
    verifyTzSetInSession(false);
  }

  @Test
  public void testRequiresValidCookieAndSSO2() throws IOException {
    when(properties.isSSO()).thenReturn(true);
    // needs valid cookie value
    req.setCookies(new Cookie("wrongName", "IST"));
    invokeHandler();
    verifyTzSetInSession(false);
  }

  private void verifyTzSetInSession(boolean isSet) {

    if (isSet) {
      assertNotNull(session.getAttribute(SessionAttributeUtils.TIMEZONE));
    } else {
      assertNull(session.getAttribute(SessionAttributeUtils.TIMEZONE));
    }
  }
}
