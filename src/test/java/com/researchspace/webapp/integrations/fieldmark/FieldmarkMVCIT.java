package com.researchspace.webapp.integrations.fieldmark;

import com.researchspace.api.v1.controller.API_MVC_TestBase;
import com.researchspace.model.User;
import com.researchspace.service.UserConnectionManager;
import com.researchspace.webapp.integrations.fieldmark.model.FieldmarkJsonExport;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

@WebAppConfiguration
public class FieldmarkMVCIT extends API_MVC_TestBase {

  private @Autowired UserConnectionManager userConnectionManager;
  private User user;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    user = createInitAndLoginAnyUser();
  }

  @Test
  @Ignore(
      "This test was used for the Fieldmark POC. "
          + "We leave the test Ignored so we can potentially run it manually by the bearer token")
  public void testCreateDatasetAndPushFile() throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();

    ////////// get access token ////////// - WITH CREDENTIAL FLOW
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    /* ************
     ///// SET THE ACCESS TOKEN MANUALLY -  It is in BitWarden /////
    **************** */
    String ACCESS_TOKEN = "-----------PASTE_TOKEN_HERE_FROM_BITWARDEN----------";
    headers = addAuthorizationHeaders(ACCESS_TOKEN);
    headers.setContentType(MediaType.APPLICATION_JSON);

    ////////// get the notebooks //////////
    String jsonResult =
        restTemplate
            .exchange(
                new URL("https://conductor.fieldmark.app/api/notebooks/").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getBody();
    System.out.println("Notebooks: " + jsonResult);

    ////////// get the records of the notebook //////////
    jsonResult =
        restTemplate
            .exchange(
                new URL("https://conductor.fieldmark.app/api/notebooks/1726126204618-rspace-igsn-demo/records").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getBody();
    System.out.println("Records in JSON: " + jsonResult);

    FieldmarkJsonExport fieldmarkRecords =
        restTemplate
            .exchange(
                new URL("https://conductor.fieldmark.app/api/notebooks/1726126204618-rspace-igsn-demo/records").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FieldmarkJsonExport.class)
            .getBody();
    System.out.println("Records in Object: " + fieldmarkRecords);

    ////////// get the CSV  //////////
    jsonResult =
        restTemplate
            .exchange(
                new URL("https://conductor.fieldmark.app/api/notebooks/1726126204618-rspace-igsn-demo/Primary.csv").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getBody();
    System.out.println("CSV: " + jsonResult);

    ////////// get the ZIP file //////////
    byte[] zipFileBytes =
        restTemplate
            .exchange(
                new URL(
                    "https://conductor.fieldmark.app/api/notebooks/1726126204618-rspace-igsn-demo/Primary.zip").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class)
            .getBody();
    System.out.println("ZIP bytes: " + zipFileBytes);
    File fileFromBytes = null;

    ///// save the photos (that were entries in the zip file) to the filesystem
    try {
      fileFromBytes = new File("file.zip");
      FileUtils.writeByteArrayToFile(fileFromBytes, zipFileBytes);

      try (ZipFile zipFile = new ZipFile(fileFromBytes)) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          // Check if entry is a directory
          if (!entry.isDirectory()) {
            FileUtils.copyInputStreamToFile
                (zipFile.getInputStream(entry), new File(entry.getName()));

            System.out.println("File \"" + entry.getName() + "\" correctly saved");
          }
        }
      }
    } finally {
      if (fileFromBytes != null) {
        FileUtils.delete(fileFromBytes);
      }
    }
  }

  private HttpHeaders addAuthorizationHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    return addAuthorizationHeaders(headers, accessToken);
  }

  private HttpHeaders addAuthorizationHeaders(HttpHeaders headers, String accessToken) {
    headers.add("Authorization", String.format("Bearer %s", accessToken));
    return headers;
  }

}
