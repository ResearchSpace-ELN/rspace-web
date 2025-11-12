package com.researchspace.webapp.controller;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.researchspace.model.Group;
import com.researchspace.model.Role;
import com.researchspace.model.User;
import com.researchspace.model.permissions.IPermissionUtils;
import com.researchspace.model.record.BaseRecord;
import com.researchspace.model.record.TestFactory;
import com.researchspace.service.GroupManager;
import com.researchspace.service.MessageSourceUtils;
import com.researchspace.service.UserManager;
import java.util.Locale;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.ui.ExtendedModelMap;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class GroupControllerTest {
  @Mock UserManager userMgr;
  @Mock GroupManager grpMgr;
  @Mock IPermissionUtils perms;
  @Mock ApplicationEventPublisher publisher;

  @InjectMocks GroupController grpController;

  User user = TestFactory.createAnyUserWithRole("pi", Role.PI_ROLE.getName());
  Group group = TestFactory.createAnyGroup(user, new User[] {});
  StaticMessageSource messages = new StaticMessageSource();

  @BeforeEach
  public void setUp() throws Exception {
    messages.addMessage("errors.maxlength", Locale.getDefault(), "toobig");
    grpController.setMessageSource(new MessageSourceUtils(messages));
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void renameGroup() {
    group.setId(1L);
    when(userMgr.getAuthenticatedUserInSession()).thenReturn(user);
    when(grpMgr.getGroup(1L)).thenReturn(group);
    AjaxReturnObject<String> response = grpController.renameGroup(new ExtendedModelMap(), 1L, "");
    assertNotNull(response.getError());

    response =
        grpController.renameGroup(
            new ExtendedModelMap(), 1L, randomAlphabetic(BaseRecord.DEFAULT_VARCHAR_LENGTH + 1));
    assertNotNull(response.getError());
    assertEquals("toobig", response.getError().getAllErrorMessagesAsStringsSeparatedBy(""));
    Mockito.verifyNoInteractions(publisher);
    verify(grpMgr, never()).saveGroup(group, false, user);

    response =
        grpController.renameGroup(
            new ExtendedModelMap(), 1L, randomAlphabetic(BaseRecord.DEFAULT_VARCHAR_LENGTH));
    assertNull(response.getError());
    verify(grpMgr).saveGroup(group, false, user);
  }
}
