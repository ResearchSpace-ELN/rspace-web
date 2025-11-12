package com.researchspace.webapp.controller;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.researchspace.model.RSMath;
import com.researchspace.model.User;
import com.researchspace.model.record.TestFactory;
import com.researchspace.service.MediaManager;
import com.researchspace.service.MessageSourceUtils;
import com.researchspace.service.UserManager;
import java.io.IOException;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.support.StaticMessageSource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class SVGMathControllerTest {
  private static final long ANY_MATH_ELEMENT_ID = 2L;

  private static final long ANY_FIELD_ID = 1L;

  private static final String ERRORS_REQUIRED = "errors.required";

  private static final String VALIDXML_BUT_WRONG_NAMESPACE =
      "<element xmlns:x=\"http://some.namespace.com\"/>";

  private static final String VALID_SVG = "<svg  xmlns=\"http://www.w3.org/2000/svg\" />";

  private static final String VALID_LATEX = "x^2";

  @Mock MediaManager mediaMgr;
  @Mock UserManager userManager;
  @InjectMocks SVGMathController svg;
  private StaticMessageSource mockMessageSource;

  @BeforeEach
  public void setUp() throws Exception {
    mockMessageSource = new StaticMessageSource();
    svg.setMessageSource(new MessageSourceUtils(mockMessageSource));
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testSaveSvg2Validate() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mockMessageSource.addMessage(ERRORS_REQUIRED, Locale.getDefault(), "need svg");
          svg.saveSvg("", ANY_FIELD_ID, VALID_LATEX, ANY_MATH_ELEMENT_ID);
        });
  }

  @Test
  public void testSaveSvg2ValidateNeedSVG() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mockMessageSource.addMessage(ERRORS_REQUIRED, Locale.getDefault(), "need latex");
          svg.saveSvg(VALID_SVG, ANY_FIELD_ID, "", ANY_MATH_ELEMENT_ID);
        });
  }

  @Test
  public void testSaveSvg2ValidatLatexTooLong() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mockMessageSource.addMessage("errors.maxlength", Locale.getDefault(), "need latex");
          svg.saveSvg(
              VALID_SVG,
              ANY_FIELD_ID,
              randomAlphabetic(RSMath.LATEX_COLUMN_SIZE + 1),
              ANY_MATH_ELEMENT_ID);
        });
  }

  @Test
  public void testSaveSvg2ValidateSVG() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mockMessageSource.addMessage("errors.invalidxml", Locale.getDefault(), "need latex");
          svg.saveSvg("not xml", ANY_FIELD_ID, VALID_LATEX, ANY_MATH_ELEMENT_ID);
        });
  }

  @Test
  public void testSaveSvg2ValidateSVGNamespace() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mockMessageSource.addMessage("errors.invalidxml.ns", Locale.getDefault(), "need latex");
          svg.saveSvg(VALIDXML_BUT_WRONG_NAMESPACE, ANY_FIELD_ID, VALID_LATEX, ANY_MATH_ELEMENT_ID);
        });
  }

  @Test
  public void testSaveSvg2OK() throws IOException {
    User user = TestFactory.createAnyUser("any");
    when(userManager.getAuthenticatedUserInSession()).thenReturn(user);
    RSMath math = TestFactory.createAMathElement();
    math.setId(2L);
    when(mediaMgr.saveMath(VALID_SVG, ANY_FIELD_ID, VALID_LATEX, 2L, user)).thenReturn(math);
    assertNotNull(svg.saveSvg(VALID_SVG, ANY_FIELD_ID, VALID_LATEX, ANY_MATH_ELEMENT_ID));
  }
}
