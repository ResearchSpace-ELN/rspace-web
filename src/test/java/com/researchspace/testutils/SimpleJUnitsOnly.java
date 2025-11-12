package com.researchspace.testutils;

import org.junit.jupiter.api.Assumptions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SimpleJUnitsOnly implements TestRule {

  @Override
  public Statement apply(Statement base, Description description) {
    if (description.getTestClass().isAssignableFrom(BaseManagerTestCaseBase.class)) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Assumptions.assumeTrue(1 != 1);
        }
      };
    } else {
      return base;
    }
  }
}
