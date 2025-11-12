package com.researchspace.webapp.controller;

import static com.researchspace.testutils.RSpaceTestUtils.assertAuthExceptionThrown;
import static org.junit.jupiter.api.Assertions.*;

import com.researchspace.licenseserver.model.License;
import com.researchspace.model.Role;
import com.researchspace.model.User;
import com.researchspace.model.record.TestFactory;
import com.researchspace.service.LicenseService;
import com.researchspace.service.MessageSourceUtils;
import com.researchspace.service.UserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class SysadminSupportControllerTest {

  @Mock LicenseService licenseService;
  @Mock UserManager usrMgr;
  @Mock MessageSourceUtils messages;

  @InjectMocks SysAdminSupportController controller;
  User sysadmin;

  @BeforeEach
  public void setUp() throws Exception {
    sysadmin = TestFactory.createAnyUser("sysadmin");
    sysadmin.addRole(Role.SYSTEM_ROLE);
    Mockito.when(usrMgr.getAuthenticatedUserInSession()).thenReturn(sysadmin);
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testOnlySysAdminCanReloadLicense() throws Exception {
    demoteSysadmin();
    assertAuthExceptionThrown(() -> controller.forceRefreshLicense());
  }

  private void demoteSysadmin() {
    sysadmin.addRole(Role.USER_ROLE);
    sysadmin.removeRole(Role.SYSTEM_ROLE);
  }

  @Test
  public void testOnlySysAdminCanViewLicense() throws Exception {
    demoteSysadmin();
    assertAuthExceptionThrown(() -> controller.getLicense());
  }

  @Test
  public void testGetLicense() {
    License license = new License();
    license.setId(23L);
    Mockito.when(licenseService.getLicense()).thenReturn(license);
    AjaxReturnObject<License> returned = controller.getLicense();
    assertEquals(23, returned.getData().getId().intValue());
  }

  @Test
  public void testReloadLicense() {
    Mockito.when(licenseService.forceRefreshLicense()).thenReturn(true, false);
    AjaxReturnObject<Boolean> rc = controller.forceRefreshLicense();
    assertTrue(rc.getData());
    // false
    rc = controller.forceRefreshLicense();
    assertFalse(rc.getData());
  }
}
