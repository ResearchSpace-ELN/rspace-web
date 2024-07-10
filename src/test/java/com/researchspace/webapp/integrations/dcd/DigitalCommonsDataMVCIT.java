package com.researchspace.webapp.integrations.dcd;

import static com.researchspace.service.IntegrationsHandler.DIGITAL_COMMONS_DATA_APP_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.researchspace.api.v1.controller.API_MVC_TestBase;
import com.researchspace.core.testutil.CoreTestUtils;
import com.researchspace.dcd.model.DcdAccessToken;
import com.researchspace.dcd.model.DcdDataset;
import com.researchspace.dcd.model.DcdFile;
import com.researchspace.model.PaginationCriteria;
import com.researchspace.model.User;
import com.researchspace.model.oauth.UserConnection;
import com.researchspace.model.record.BaseRecord;
import com.researchspace.service.UserConnectionManager;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@WebAppConfiguration
public class DigitalCommonsDataMVCIT extends API_MVC_TestBase {

  private RestTemplate restTemplate;
  private @Autowired DigitalCommonsDataManager digitalCommonsDataManager;
  private @Autowired UserConnectionManager userConnectionManager;

  private User user;
  private PaginationCriteria<BaseRecord> pgcrit = null;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    user = createInitAndLoginAnyUser();
    pgcrit = PaginationCriteria.createDefaultForClass(BaseRecord.class);
  }

  @Test
  public void testGetAccessToken() throws Exception {
    MvcResult result =
        mockMvc
            .perform(post("/apps/dcd/connect").principal(user::getUsername))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals("connect/dcd/connected", result.getModelAndView().getViewName());

    String assessToken =
        userConnectionManager
            .findByUserNameProviderName(user.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME)
            .get()
            .getAccessToken();
    assertNotNull(assessToken);
    assertFalse(assessToken.isBlank());
  }

  @Test
  // TODO[nik]: Still needs to be implemented
  public void testGetDatasetsAndByIdCredentialFlow() throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();

    ////////// get access token ////////// - WITH CREDENTIAL FLOW
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(
        "kW1WtsEeW7b9LugvT-jq9148gmkTPDeFFZe4NM9mV1k",
        "Q5QHqNn91Q2tvJwsqkRDorAS9i6Mg685GLY6TYLBWH22MITQ4CvOnRx3kQ43IKAr");

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "client_credentials");
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

    DcdAccessToken dcdAccessToken =
        restTemplate
            .exchange(
                "https://auth.data.mendeley.com/oauth2/token",
                HttpMethod.POST,
                request,
                DcdAccessToken.class)
            .getBody();
    System.out.println("AccessToken: " + dcdAccessToken);

    ////////// get the datasets //////////
    String jsonResult =
        restTemplate
            .exchange(
                new URL("https://api.data.mendeley.com/datasets").toURI(),
                //       new URL("http://localhost:80").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(addAuthorizationHeaders(dcdAccessToken.getAccessToken())),
                String.class)
            .getBody();
    System.out.println("Datasets: " + jsonResult);

    ////////// get the dataset with ID = jxj8bn2vyf //////////
    jsonResult =
        restTemplate
            .exchange(
                "https://api.data.mendeley.com/datasets/jxj8bn2vyf",
                //       new URL("http://localhost:80").toURI(),
                HttpMethod.GET,
                new HttpEntity<>(addAuthorizationHeaders(dcdAccessToken.getAccessToken())),
                String.class)
            .getBody();
    System.out.println("Dataset[jxj8bn2vyf]: " + jsonResult);

    /////// create a draft dataset   ////////
    // TODO[nik]
    headers = addAuthorizationHeaders(dcdAccessToken.getAccessToken());
    headers.setContentType(MediaType.APPLICATION_JSON);
    jsonResult =
        restTemplate
            .exchange(
                // "https://api.data.mendeley.com/active-data-entities/datasets/drafts",
                new URL("http://localhost:80").toURI(),
                HttpMethod.POST,
                new HttpEntity<>(
                    "{\n"
                        + "  \"ancestors\": [\n"
                        + "    {\n"
                        + "      \"parent\": {}\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"parent\": {\n"
                        + "    \"parent\": {}\n"
                        + "  },\n"
                        + "  \"empty\": true,\n"
                        + "  \"name\": \"test_nik_code_1\",\n"
                        + "  \"description\": \"test_nik_code_1\",\n"
                        + "  \"method\": \"post\",\n"
                        + "  \"is_confidential\": true,\n"
                        + "  \"property1\": {},\n"
                        + "  \"property2\": {}\n"
                        + "}",
                    headers),
                String.class)
            .getBody();
    System.out.println("Create draft dataset: " + jsonResult);

    /////// upload a file into the dataset  //////////
    // TODO[nik]

    ////////// upload a file /////////////
    //    FileSystemResource file =
    //        new FileSystemResource(new File("src/test/resources/TestResources/Picture1.png"));
    ////    File file =
    ////        new File("src/test/resources/TestResources/csv.csv");
    //
    //    ContentDisposition contentDisposition =   ContentDisposition.builder("attachment")
    //        .name("Picture1.png").filename("Picture1.png").build();
    //
    //    headers = new HttpHeaders();
    //    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    //    headers.setContentLength(file.contentLength());
    //    headers.setContentDisposition(contentDisposition);
    //    headers.add("Authorization",
    //        String.format("Bearer %s", accessToken.getAccessToken()));
    //
    //    jsonResult = restTemplate.exchange(
    //        "https://uploads.data.mendeley.com/uploads",
    ////        "http://localhost:80"),
    //        HttpMethod.POST,
    //        new HttpEntity<>(file, headers),
    //        String.class
    //    ).getBody();

    //// another method
    //    jsonResult = restTemplate.postForEntity("https://uploads.data.mendeley.com/uploads",
    //        uploadRequestEntity,
    //        String.class
    //    ).getBody();

    /// another method
    //    byte[] fileContent = FileUtils.readFileToByteArray(
    //        new File("src/test/resources/TestResources/Picture1.png"));
    //    String base64Image = Base64.getEncoder().encodeToString(fileContent);
    //    jsonResult = restTemplate.postForEntity(
    ////        "http://localhost:80",
    //        "https://uploads.data.mendeley.com/uploads",
    //        constructRequest(base64Image, accessToken.getAccessToken(), file.contentLength()),
    //        String.class).getBody();

    //    System.out.println("Upload file: " + jsonResult);
  }

  @Test
  public void testDatasetsWithAuthorizationCodeFlow() throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();

    ////////// get access token ////////// - WITH Using Authorization Code flow
    //    HttpHeaders headers = new HttpHeaders();
    //    // headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    //
    //    String secretRandomWord = "matthiasandnico";
    //
    //    //
    // "https://auth.data.mendeley.com/oauth2/authorize?response_type=code&client_id=kW1WtsEeW7b9LugvT-jq9148gmkTPDeFFZe4NM9mV1k&redirect_uri=https%3A%2F%2Fresearchspace.eu.ngrok.io%2Fapps%2Fdigitalcommonsdata%2Fcallback&scope=openid%20profile%20email%20dcd:profile&state=matthiasandnico
    //    String result =
    //        restTemplate
    //            .exchange(
    //                "http://auth.data.mendeley.com/oauth2/authorize"
    //                    // "http://localhost:8090/oauth2/authorize"
    //                    +
    // "?response_type=code&client_id=kW1WtsEeW7b9LugvT-jq9148gmkTPDeFFZe4NM9mV1k"
    //                    +
    // "&redirect_uri=https%3A%2F%2Fresearchspace.eu.ngrok.io%2Fapps%2Fdigitalcommonsdata%2Fcallback"
    //                    + "&scope=openid%20profile%20email%20dcd:profile&state="
    //                    + secretRandomWord,
    //                HttpMethod.GET,
    //                new HttpEntity<>(null),
    //                String.class)
    //            .getBody();
    //    System.out.println("result: " + result);

    //        //////// get the datasets //////////
    //        String jsonResult =
    //            restTemplate
    //                .exchange(
    //                    new URL("https://api.data.mendeley.com/datasets").toURI(),
    //                    //       new URL("http://localhost:80").toURI(),
    //                    HttpMethod.GET,
    //                    new HttpEntity<>(getHttpHeaders(accessToken.getAccessToken())),
    //                    String.class)
    //                .getBody();
    //        System.out.println("Datasets: " + jsonResult);
    //
    //        ////////// get the dataset with ID = jxj8bn2vyf //////////
    //        jsonResult =
    //            restTemplate
    //                .exchange(
    //                    "https://api.data.mendeley.com/datasets/jxj8bn2vyf",
    //                    //       new URL("http://localhost:80").toURI(),
    //                    HttpMethod.GET,
    //                    new HttpEntity<>(getHttpHeaders(accessToken.getAccessToken())),
    //                    String.class)
    //                .getBody();
    //        System.out.println("Dataset[jxj8bn2vyf]: " + jsonResult);

  }

  @Test
  public void testCreateDatasetAndPushFile() throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();

    ////////// get access token ////////// - WITH CREDENTIAL FLOW
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    /* ************
     ///// 1. GET THE ACCESS TOKEN MANUALLY /////
    **************** */
    int randomInt = (int) (Math.random() * 5000 + 1);
    //    /////// create a draft dataset   //////// OK
    String ACCESS_TOKEN =
        "eyJraWQiOiJHWmY1d1l3WFlSeElta1RXWExndllENVFKQ0p3UEFiQjAyVUFydENxSjFVIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIyOTc4MjA1MTkiLCJhdWQiOiJrVzFXdHNFZVc3YjlMdWd2VC1qcTkxNDhnbWtUUERlRkZaZTROTTltVjFrIiwibmJmIjoxNzIyNDM3MjUxLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwiZW1haWwiLCJkY2Q6cHJvZmlsZSJdLCJpc3MiOiJodHRwczovL2F1dGguZGF0YS5tZW5kZWxleS5jb20iLCJkY2Q6cm9sZXMiOltdLCJleHAiOjE3MjI0Mzc1NTEsImRjZDpwcm9maWxlX2lkIjoiNWY2ODE4NGUtNDQwYS00NGVjLTk1OGYtMjVjZDIxNmFmODgyIiwiaWF0IjoxNzIyNDM3MjUxLCJkY2Q6Y3VzdG9tZXJfaWQiOiI3Mzc3OTUyYy02NDA0LTQ0MjgtYmJmYy03NzI0MmNmYzMwMWUifQ.cgQhliZmrER12OhGZChPU4CO0C0gK_29ancrGlqY20_OaxiB22isf3g02ykXPJxU0MypYPTvctsr1EOKJzMdf-ysRESLglPsD6Nb-Zl76UagQxszUWFIrvMAzrDXRFrewdsBkf8Dei38hdozhYwgycOZNoGlTrCbIoCPNVgN3IC9-WaGkJpwWpLqEeatjt4xh7uo13IbOT52z3ArKW_J1Q5Ixqvh_8nnhaIVGG7Ny_gZP03I9T4--y9pUvCRpWbT5m_u1YwTRYg9m1kBcEJ60CeYgd_whnCXFe_YF1GkbHamktDDrE-PXFzoJ8F3kcPzmsme5et9_gV2t3HTMXoypw";
    headers = addAuthorizationHeaders(ACCESS_TOKEN);
    headers.setContentType(MediaType.APPLICATION_JSON);
    DcdDataset datasetResult =
        restTemplate
            .exchange(
                "https://api.data.mendeley.com/active-data-entities/datasets/drafts",
                // new URL("http://localhost:80").toURI(),
                HttpMethod.POST,
                new HttpEntity<>(
                    "{\n"
                        + "  \"ancestors\": [\n"
                        + "    {\n"
                        + "      \"parent\": {}\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"parent\": {\n"
                        + "    \"parent\": {}\n"
                        + "  },\n"
                        + "  \"empty\": true,\n"
                        + "  \"name\": \"test_nik_code_"
                        + randomInt
                        + "\",\n"
                        + "  \"description\": \"test_nik_code_"
                        + randomInt
                        + "\",\n"
                        + "  \"is_confidential\": false"
                        + "}",
                    headers),
                DcdDataset.class)
            .getBody();
    System.out.println("Create draft dataset: " + datasetResult);

    ///// CREATE a file   //////////
    //     TODO[nik]
    FileSystemResource file =
        new FileSystemResource(new File("src/test/resources/TestResources/Picture1.png"));
    //    File file =
    //        new File("src/test/resources/TestResources/csv.csv");

    //        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
    //            .name("Picture1.png").filename("Picture1.png").build();

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentLength(file.contentLength());
    //        headers.setContentDisposition(contentDisposition);

    DcdFile dcdFileResult =
        restTemplate
            .exchange(
                "https://uploads.data.mendeley.com/uploads",
                //        "http://localhost:80"),
                HttpMethod.POST,
                new HttpEntity<>(file, addAuthorizationHeaders(headers, ACCESS_TOKEN)),
                DcdFile.class)
            .getBody();
    System.out.println("Upload file result: " + dcdFileResult);

    ///////// LINK THE FILE TO THE DATASET ///////////
    headers = addAuthorizationHeaders(ACCESS_TOKEN);
    headers.setContentType(MediaType.APPLICATION_JSON);
    String linkDatasetResult =
        restTemplate
            .exchange(
                "https://api.data.mendeley.com/active-data-entities/datasets/drafts/"
                    + datasetResult.getId()
                    + "/files",
                // new URL("http://localhost:80").toURI(),
                HttpMethod.POST,
                new HttpEntity<>(
                    "{\n"
                        + "  \"ancestors\": [\n"
                        + "    {\n"
                        + "      \"parent\": {}\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"parent\": {\n"
                        + "    \"parent\": {}\n"
                        + "  },\n"
                        + "  \"ticket_id\": \""
                        + dcdFileResult.getId()
                        + "\",\n"
                        + "  \"filename\": \"Picture1.png\",\n"
                        + "  \"description\": \"immagine prova\",\n"
                        + "  \"media_type\": \"image/png\"\n"
                        + "}",
                    headers),
                String.class)
            .getBody();
    System.out.println("Link Dataset and File result: " + linkDatasetResult);

    //    ------------------

    //    // another method
    //    jsonResult = restTemplate.postForEntity("https://uploads.data.mendeley.com/uploads",
    //        uploadRequestEntity,
    //        String.class
    //    ).getBody();

    //    // another method
    //    byte[] fileContent = FileUtils.readFileToByteArray(
    //        new File("src/test/resources/TestResources/Picture1.png"));
    //    String base64Image = Base64.getEncoder().encodeToString(fileContent);
    //    jsonResult = restTemplate.postForEntity(
    //        //        "http://localhost:80",
    //        "https://uploads.data.mendeley.com/uploads",
    //        constructRequest(base64Image, accessToken.getAccessToken(), file.contentLength()),
    //        String.class).getBody();
    //
    //    System.out.println("Upload file: " + jsonResult);
  }

  @Test
  public void testConnectController() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/apps/digitalcommonsdata/connect").principal(user::getUsername))
            .andExpect(status().is3xxRedirection())
            .andReturn();

    String redirectUrlString = result.getResponse().getRedirectedUrl();
    Map<String, String> parameters = getUrlQueryMap(redirectUrlString);

    assertNotNull(redirectUrlString);
    Optional<UserConnection> optUserConn =
        userConnectionManager.findByUserNameProviderName(
            user.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME);
    assertTrue(optUserConn.isPresent());
    assertFalse(optUserConn.get().getSecret().isBlank());
    result =
        mockMvc
            .perform(
                get("/apps/digitalcommonsdata/callback")
                    .param("code", parameters.get("code"))
                    .param("state", parameters.get("state"))
                    .principal(user::getUsername))
            .andExpect(status().is3xxRedirection())
            .andReturn();
    redirectUrlString = result.getResponse().getRedirectedUrl();

    assertTrue(redirectUrlString.endsWith("connected"));
    optUserConn =
        userConnectionManager.findByUserNameProviderName(
            user.getUsername(), DIGITAL_COMMONS_DATA_APP_NAME);
    assertTrue(optUserConn.isPresent());
    assertFalse(optUserConn.get().getSecret().isBlank());
    assertFalse(optUserConn.get().getAccessToken().startsWith("TEMP"));
  }

  private HttpEntity<MultiValueMap<String, Object>> constructRequest(
      String base64Image, String accessToken, long size) throws IOException {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        Base64Util.convertToHttpEntity(
            Base64Util.generateFilename(base64Image), Base64Util.stripStartBase64(base64Image)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setContentLength(5);
    headers.add("Authorization", String.format("Bearer %s", accessToken));

    return new HttpEntity<>(body, headers);
  }

  @Test
  @Ignore
  public void testUploadFile() throws Exception {
    User user = createAndSaveUser(CoreTestUtils.getRandomName(8));
    logoutAndLoginAs(user);
    MockMultipartFile mf =
        new MockMultipartFile(
            "imageFile", "image.png", "png", getTestResourceFileStream("Picture1.png"));
    long size = mf.getSize();
    mockMvc
        .perform(
            fileUpload("/userform/profileImage/upload")
                .file(mf)
                .principal(new MockPrincipal(user.getUsername())))
        .andExpect(status().isOk())
        .andReturn();
  }

  private HttpHeaders addAuthorizationHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    //    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("Authorization", String.format("Bearer %s", accessToken));
    return headers;
  }

  private HttpHeaders addAuthorizationHeaders(HttpHeaders headers, String accessToken) {
    headers.add("Authorization", String.format("Bearer %s", accessToken));
    return headers;
  }

  public static Map<String, String> getUrlQueryMap(String query) {
    String[] params = query.split("&");
    Map<String, String> map = new HashMap<String, String>();

    for (String param : params) {
      String name = param.split("=")[0];
      String value = param.split("=")[1];
      map.put(name, value);
    }
    return map;
  }
}
