package com.researchspace.webapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.researchspace.Constants;
import com.researchspace.comms.CommunicationTargetFinderPolicy;
import com.researchspace.comms.StrictPermissionCheckingRecipientFinderPolicy;
import com.researchspace.core.util.ISearchResults;
import com.researchspace.core.util.TransformerUtils;
import com.researchspace.model.PaginationCriteria;
import com.researchspace.model.User;
import com.researchspace.model.comms.CalendarEvent;
import com.researchspace.model.comms.MessageType;
import com.researchspace.model.comms.MsgOrReqstCreationCfg;
import com.researchspace.model.dtos.IControllerInputValidator;
import com.researchspace.model.permissions.IPermissionUtils;
import com.researchspace.model.record.TestFactory;
import com.researchspace.service.CommunicationManager;
import com.researchspace.service.MessageOrRequestCreatorManager;
import com.researchspace.service.UserManager;
import com.researchspace.service.impl.PermissionsUtilsStub;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.shiro.authz.AuthorizationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class MessageAndRequestControllerTest {

  @InjectMocks MessageAndRequestController ctrller;

  @Mock CommunicationTargetFinderPolicy comTargetPolicy;
  @Mock UserManager usrMgr;
  @Mock ISearchResults<User> results;
  @Mock CommunicationManager mgr;
  @Mock IControllerInputValidator validator;
  @Mock ApplicationContext context;
  @Mock MessageOrRequestCreatorManager reqCreateMgr;
  MockHttpServletRequest request;
  IPermissionUtils permUtilsStub;

  //
  @BeforeEach
  public void setUp() throws Exception {
    permUtilsStub = new PermissionsUtilsStub();
    request = new MockHttpServletRequest();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGetMessageTypesFromStringTypesRejectsNonAdminSendingToAll() {
    assertThrows(
        AuthorizationException.class,
        () -> {
          User notAdmin = TestFactory.createAnyUserWithRole("sender", Constants.PI_ROLE);
          User recipient = TestFactory.createAnyUser("target");
          MsgOrReqstCreationCfg cfg = new MsgOrReqstCreationCfg(notAdmin, permUtilsStub);
          cfg.setMessageType(MessageType.GLOBAL_MESSAGE);

          BindingResult result = new BeanPropertyBindingResult(cfg, "cfg");
          ctrller.getUsernamesFromInput(
              notAdmin, cfg, result, comTargetPolicy, TransformerUtils.toSet(recipient));
        });
  }

  @Test
  public void testSendToAllHandledIfNoValidRecipients() {
    final User admin = TestFactory.createAnyUserWithRole("sender", Constants.ADMIN_ROLE);
    final User recipient = TestFactory.createAnyUser("target");
    MsgOrReqstCreationCfg cfg = new MsgOrReqstCreationCfg(admin, permUtilsStub);

    cfg.setMessageType(MessageType.GLOBAL_MESSAGE);
    BindingResult result = new BeanPropertyBindingResult(cfg, "cfg");
    when(usrMgr.getViewableUsers(
            admin, PaginationCriteria.createDefaultForClass(User.class).setGetAllResults()))
        .thenReturn(results);
    when(results.getResults()).thenReturn(Collections.EMPTY_LIST);
    when(usrMgr.getUserByUsername("target")).thenReturn(recipient);

    Set<String> users =
        ctrller.getUsernamesFromInput(
            admin, cfg, result, comTargetPolicy, TransformerUtils.toSet(recipient));
    assertTrue(users.isEmpty());
  }

  @Test
  public void testGetMessageTypesFromStringTypesHandlesAdminSendingToAll() {
    final User admin = TestFactory.createAnyUserWithRole("sender", Constants.ADMIN_ROLE);
    final User recipient = TestFactory.createAnyUser("target");
    MsgOrReqstCreationCfg cfg = new MsgOrReqstCreationCfg(admin, permUtilsStub);
    cfg.setMessageType(MessageType.GLOBAL_MESSAGE);
    BindingResult result = new BeanPropertyBindingResult(cfg, "cfg");

    when(usrMgr.getViewableUsers(
            admin, PaginationCriteria.createDefaultForClass(User.class).setGetAllResults()))
        .thenReturn(results);
    when(results.getResults()).thenReturn(TransformerUtils.toList(recipient));
    when(usrMgr.getUserByUsername("target")).thenReturn(recipient);

    Set<String> users =
        ctrller.getUsernamesFromInput(
            admin, cfg, result, comTargetPolicy, TransformerUtils.toSet(recipient));
    assertEquals(1, users.size());
  }

  @Test
  public void creatCalendarEntry() throws ValidationException, IOException {
    CalendarEvent event = new CalendarEvent();
    event.setStart("2020-05-19T13:00:00Z");
    event.setEnd("2020-05-19T15:00:00Z");
    assertTrue(ctrller.createCalendarEvent(event, request).getData());

    // set start after end fails
    event.setStart("2020-05-19T17:00:00Z");
    AjaxReturnObject<Boolean> aro = ctrller.createCalendarEvent(event, request);
    assertNull(aro.getData());
    assertEquals(1, aro.getError().getErrorMessages().size());

    String icString =
        (String) request.getSession().getAttribute(MessageAndRequestController.CALENDAR_FILE_BODY);
    System.err.println(icString);
  }

  @Test
  public void createMsgDirect() {
    User subject = TestFactory.createAnyUser("creator");
    when(usrMgr.getAuthenticatedUserInSession()).thenReturn(subject);
    // no recipients
    MsgOrReqstCreationCfg creator = new MsgOrReqstCreationCfg();

    assertNull(ctrller.saveMessageDirect(creator).getData());
    User recipient = TestFactory.createAnyUser("recipient");
    creator.setRecipientnames(recipient.getUsername());
    creator.setOptionalMessage("hello");
    creator.setMessageType(MessageType.SIMPLE_MESSAGE);
    creator.setTargetFinderPolicy("STRICT");
    StrictPermissionCheckingRecipientFinderPolicy policy =
        new StrictPermissionCheckingRecipientFinderPolicy();
    when(context.getBean("strictTargetFinderPolicy", CommunicationTargetFinderPolicy.class))
        .thenReturn(policy);
    when(usrMgr.getUserByUsername(recipient.getUsername())).thenReturn(null);
    assertNull(ctrller.saveMessageDirect(creator).getData());
    when(usrMgr.getUserByUsername(recipient.getUsername())).thenReturn(recipient);
    when(mgr.getPotentialRecipientsOfRequest(
            null, creator.getMessageType(), subject.getUsername(), null, policy))
        .thenReturn(TransformerUtils.toSet(recipient));
    assertNotNull(ctrller.saveMessageDirect(creator).getData());
  }
}
