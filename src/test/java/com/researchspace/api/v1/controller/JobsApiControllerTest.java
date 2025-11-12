package com.researchspace.api.v1.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.researchspace.api.v1.model.ApiExportJobResult;
import com.researchspace.api.v1.model.ApiJob;
import com.researchspace.api.v1.model.ApiLinkItem;
import com.researchspace.core.testutil.CoreTestUtils;
import com.researchspace.model.User;
import com.researchspace.model.record.TestFactory;
import com.researchspace.properties.IPropertyHolder;
import com.researchspace.service.aws.S3Utilities;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class JobsApiControllerTest {
  @Mock JobsApiHandler handler;
  @Mock IPropertyHolder props;
  @Mock S3Utilities s3Utils;
  @InjectMocks JobsApiController controller;
  User exporter = TestFactory.createAnyUser("any");
  MockHttpServletResponse response;

  @BeforeEach
  public void setUp() throws Exception {
    response = new MockHttpServletResponse();
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testS3enabledDownloadLinkSuccess() throws BindException, MalformedURLException {
    ApiJob completed = createCompletedApiJob();
    controller.hasS3Access = true;
    when(handler.getJob(1L, exporter)).thenReturn(completed);
    URL expectedLink = new URL("https://somelink.com");
    when(s3Utils.getPresignedUrlForArchiveDownload(completed.getResourceLocation()))
        .thenReturn(expectedLink);

    ApiJob job = controller.get(1L, response, exporter);
    assertNotNull(job);
    assertTrue(job.isCompleted());
    assertTrue(job.getLinks().iterator().next().getLink().equals(expectedLink.toString()));
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  @Test
  public void testS3enabledDownloadLinkFailure() throws BindException, MalformedURLException {
    ApiJob completed = createCompletedApiJob();
    controller.hasS3Access = true;
    when(handler.getJob(1L, exporter)).thenReturn(completed);
    when(s3Utils.getPresignedUrlForArchiveDownload(completed.getResourceLocation()))
        .thenReturn(null);
    CoreTestUtils.assertIllegalStateExceptionThrown(() -> controller.get(1L, response, exporter));
  }

  @Test
  public void testGetCompleteJob() throws BindException {
    ApiJob completed = createCompletedApiJob();
    Mockito.when(handler.getJob(1L, exporter)).thenReturn(completed);

    ApiJob job = controller.get(1L, response, exporter);
    assertNotNull(job);
    assertTrue(job.isCompleted());

    assertTrue(job.getLinks().iterator().next().getRel().equals(ApiLinkItem.ENCLOSURE_REL));
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  @Test
  public void testGetIncompleteJob() throws BindException {
    ApiJob incompleted = createIncompleteApiJob();
    Mockito.when(handler.getJob(1L, exporter)).thenReturn(incompleted);

    ApiJob job = controller.get(1L, response, exporter);
    assertNotNull(job);
    assertFalse(job.isCompleted());
    assertNull(response.getHeader("Location"));
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  private ApiJob createCompletedApiJob() {
    return new ApiJob(1L, "STARTED", 0.0, new ApiExportJobResult(), true, "something.txt");
  }

  private ApiJob createIncompleteApiJob() {
    return new ApiJob(1L, "FAILED");
  }
}
