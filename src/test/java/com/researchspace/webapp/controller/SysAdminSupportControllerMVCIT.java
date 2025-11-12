package com.researchspace.webapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.researchspace.model.User;
import com.researchspace.service.impl.ConfigurableLogger;
import com.researchspace.testutils.RunProfileTestConfiguration;
import java.util.Map;
import org.apache.shiro.authz.AuthorizationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@WebAppConfiguration()
@RunProfileTestConfiguration
public class SysAdminSupportControllerMVCIT extends MVCTestBase {
  @Mock private Logger log;

  @BeforeEach
  public void setup() throws Exception {
    super.setUp();
    getBeanOfClass(ConfigurableLogger.class).setLogger(log);
  }

  @AfterEach
  public void teardown() throws Exception {
    getBeanOfClass(ConfigurableLogger.class).setLoggerDefault();
  }

  @Test
  public void testLoadPage() throws Exception {
    mockMvc
        .perform(get("/system/support"))
        .andExpect(view().name(SysAdminSupportController.SYSTEM_SUPPORT_PAGE));
  }

  @Test
  public void testGetLogs() throws Exception {
    User sysadmin = logoutAndLoginAsSysAdmin();
    MvcResult result =
        mockMvc.perform(get("/system/support/ajax/viewLog").param("numLines", "5")).andReturn();
    Map data = mvcUtils.parseJSONObjectFromResponseStream(result);
    assertNotNull(data.get("data"));
    assertNull(data.get("errorMsg"));
  }

  @Test
  public void testMailLogs() throws Exception {
    User sysadmin = logoutAndLoginAsSysAdmin();
    MvcResult result =
        mockMvc
            .perform(
                post("/system/support/ajax/mailLog")
                    .param("numLines", "5")
                    .param("message", "XXXX")
                    .principal(sysadmin::getUsername))
            .andReturn();
    Map data = mvcUtils.parseJSONObjectFromResponseStream(result);
    assertNotNull(data.get("data"));
    assertNull(data.get("errorMsg"));
    assertTrue(Boolean.parseBoolean(data.get("data").toString()));

    Thread.sleep(100); // to tackle randomly failing test
    String opsExpectedEmail = "support@<your_server>.com"; // from deployment.properties
    verify(log)
        .info(
            Mockito.anyString(),
            Mockito.eq(true),
            Mockito.contains("Log"),
            Mockito.contains(opsExpectedEmail),
            Mockito.contains("XXXX"));
  }

  @Test
  public void testMailLogsRequiresSysadminRole() throws Exception {
    User admin = logoutAndLoginAsCommunityAdmin();
    MvcResult result =
        mockMvc
            .perform(
                post("/system/support/ajax/mailLog")
                    .param("numLines", "5")
                    .param("message", "XXXX")
                    .principal(admin::getUsername))
            .andReturn();
    assertTrue(result.getResolvedException() instanceof AuthorizationException);
  }
}
