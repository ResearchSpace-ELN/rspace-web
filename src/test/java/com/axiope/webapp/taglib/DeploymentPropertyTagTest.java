package com.axiope.webapp.taglib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.researchspace.properties.IMutablePropertyHolder;
import com.researchspace.properties.PropertyHolder;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeploymentPropertyTagTest {
  IMutablePropertyHolder propertySource;
  DeploymentPropertyTagTSS tagTSS;
  DeploymentPropertyTag tag;

  class DeploymentPropertyTagTSS extends DeploymentPropertyTag {
    // retrieves property source
    Object getPropertyHolderFromServetContext() {
      return propertySource;
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    tagTSS = new DeploymentPropertyTagTSS();
    tag = tagTSS;
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void nonBlankOnlyDoesNotCompareValue() throws JspException {
    propertySource = new PropertyHolder();
    propertySource.setUserSignupCode("xyz");
    tag.setTestNonBlankOnly(true);
    tag.setName("userSignupCode");
    assertTagIncluded();
    tag.setValue("ignored");
    assertTagIncluded();

    propertySource.setUserSignupCode(" ");
    assertTagSkipped();
  }

  private void assertTagIncluded() throws JspException {
    assertEquals(TagSupport.EVAL_BODY_INCLUDE, tag.doStartTag());
  }

  @Test
  public void testDoStartTagMatchTrue() throws JspException {
    // set up properties
    propertySource = new PropertyHolder();
    propertySource.setCloud("true");
    // happy case
    tag.setName("cloud");
    tag.setValue("true");
    assertTagIncluded();

    // wrong value doesn't match
    tag.setValue("false");
    assertTagSkipped();

    // unknown property handled gracefully and skipped
    tag.setName("notAProperty");
    tag.setValue("true");
    assertTagSkipped();
  }

  private void assertTagSkipped() throws JspException {
    assertEquals(TagSupport.SKIP_BODY, tag.doStartTag());
  }

  @Test
  public void testDoStartTagMatchFalse() throws JspException {
    // set up properties
    propertySource = new PropertyHolder();
    propertySource.setCloud("true");
    // happy case
    tag.setName("cloud");
    tag.setValue("true");
    tag.setMatch(false);
    assertTagSkipped();

    // wrong value doesn't match
    tag.setValue("false");
    assertTagIncluded();

    // unknown property handled gracefully and included, as is not matched
    tag.setName("notAProperty");
    tag.setValue("true");
    assertTagIncluded();
  }
}
