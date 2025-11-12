package com.researchspace.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.comms.MessageType;
import com.researchspace.testutils.SpringTransactionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class JoinGroupRequestHandlerTest extends SpringTransactionalTest {

  @Autowired()
  @Qualifier("joinGroupRequestHandler")
  private RSpaceRequestUpdateHandler handler;

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testHandleRequest() {
    assertTrue(handler.handleRequest(MessageType.REQUEST_JOIN_LAB_GROUP));
    assertFalse(handler.handleRequest(MessageType.REQUEST_JOIN_EXISTING_COLLAB_GROUP));
  }
}
