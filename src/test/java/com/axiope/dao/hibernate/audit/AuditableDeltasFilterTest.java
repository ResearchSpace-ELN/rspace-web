package com.axiope.dao.hibernate.audit;

import com.researchspace.model.record.StructuredDocument;
import com.researchspace.model.record.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditableDeltasFilterTest {

  ObjectAuditFilter filter = new AuditableDeltasFilter();

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  void testUnattachedFieldFilteredOut() {
    // creating a document is noteworthy and should be audited
    StructuredDocument sd = TestFactory.createAnySD();
    Assertions.assertTrue(filter.filter(sd));
    // clear updates
    sd.clearDelta();
    // should now fail
    Assertions.assertFalse(filter.filter(sd));

    // renaming is noteworthy
    sd.setName("name");
    Assertions.assertTrue(filter.filter(sd));
    sd.clearDelta();
    Assertions.assertFalse(filter.filter(sd));

    // so is updating a field's data
    sd.getFields().get(0).setFieldData("abc");
    Assertions.assertTrue(filter.filter(sd));
    sd.clearDelta();
    Assertions.assertFalse(filter.filter(sd));

    // but setting a temp record isn't;
    sd.setTempRecord(sd.copy());
    Assertions.assertFalse(filter.filter(sd));
  }
}
