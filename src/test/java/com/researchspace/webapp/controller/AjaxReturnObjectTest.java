package com.researchspace.webapp.controller;

import com.researchspace.model.field.ErrorList;
import org.junit.jupiter.api.Test;

class AjaxReturnObjectTest {

  @Test
  void testAjaxReturnObjectNullErrorOK() {
    new AjaxReturnObject<String>("data", null);
  }

  @Test
  void testAjaxReturnObjectNullDataOK() {
    ErrorList el = new ErrorList();
    new AjaxReturnObject<String>(null, el);
  }
}
