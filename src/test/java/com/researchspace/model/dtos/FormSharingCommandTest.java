package com.researchspace.model.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.researchspace.model.AccessControl;
import com.researchspace.model.permissions.PermissionType;
import com.researchspace.model.record.RSForm;
import com.researchspace.model.record.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormSharingCommandTest {

  RSForm form;

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testFormSharingCommandTemplate() {
    form = TestFactory.createAnyForm("X");
    form.setId(1L);
    assertNotNull(form.getAccessControl());
    FormSharingCommand tsc = new FormSharingCommand(form);
    assertEquals(1L, tsc.getFormId().longValue());
    assertEquals(PermissionType.NONE.toString(), tsc.getGroupOptions().get(0));
    assertEquals(PermissionType.NONE.toString(), tsc.getWorldOptions().get(0));
  }

  @Test
  public void testToAccessControl() {
    form = TestFactory.createAnyForm("X");
    form.setId(1L);
    assertNotNull(form.getAccessControl());
    FormSharingCommand tsc = new FormSharingCommand(form);
    AccessControl ac = tsc.toAccessControl();
    assertNotNull(ac.getGroupPermissionType());
    assertNotNull(ac.getWorldPermissionType());
    assertNotNull(ac.getOwnerPermissionType());
  }
}
