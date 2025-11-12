package com.researchspace.model.audittrail;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.User;
import com.researchspace.model.dtos.ShareConfigElement;
import com.researchspace.model.record.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShareAuditEventTest {
  User user = com.researchspace.model.record.TestFactory.createAnyUser("any");
  User sharee = com.researchspace.model.record.TestFactory.createAnyUser("sharee");
  Record shared = com.researchspace.model.record.TestFactory.createAnyRecord(user);

  @BeforeEach
  public void setUp() throws Exception {
    shared.setId(1L);
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGetOriginalToCopy() {
    ShareConfigElement element = new ShareConfigElement(1234L, "write");
    ShareRecordAuditEvent event =
        new ShareRecordAuditEvent(user, shared, new ShareConfigElement[] {element});
    assertTrue(event.getAuditData().getData().containsKey("sharing"));
  }
}
