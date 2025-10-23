package com.researchspace.webapp.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.researchspace.model.User;
import com.researchspace.model.record.IllegalAddChildOperation;
import com.researchspace.model.record.Snippet;
import com.researchspace.service.RecordManager;
import com.researchspace.service.UserManager;
import com.researchspace.testutils.SpringTransactionalTest;
import java.security.Principal;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.ObjectRetrievalFailureException;

public class SnippetControllerTest extends SpringTransactionalTest {

  @Autowired private SnippetController snippetController;

  @Autowired private RecordManager recordManager;

  @Autowired private UserManager userManager;

  private MockHttpServletResponse response;

  private User user;
  private Principal principalTestUserStub = null;

  @Before
  public void setUp() throws IllegalAddChildOperation {
    User user = createAndSaveUserIfNotExists(getRandomAlphabeticString("any"));
    initialiseContentWithExampleContent(user);
    assertTrue(user.isContentInitialized());
    principalTestUserStub = new MockPrincipal(user.getUsername());
  }

  @Test
  public void testCreateNewSimpleSnippet() throws Exception {
    AjaxReturnObject<String> response =
        snippetController.createSnippet("a", "b", 0L, principalTestUserStub);
    String createResultMsg = response.getData();
    assertEquals(createResultMsg, messages.getMessage("snippet.creation.ok", new String[] {"a"}));

    // test invalid names
    String invalidName = "<img src=\"image.png\" onerror=\"alert('1');\">";
    response = snippetController.createSnippet(invalidName, "b", 0L, principalTestUserStub);
    assertNull(response.getData());
    assertEquals(
        response.getErrorMsg().getAllErrorMessagesAsStringsSeparatedBy(""),
        messages.getMessage("errors.invalidchars", new String[] {"/,> or <", "name"}));
  }

  private static final Long UNEXISTING_SNIPPET_ID = -501L;

  @Test
  public void testGetSnippetContent() {

    final String testContent = "test snippet content";

    User user = userManager.getUserByUsername(principalTestUserStub.getName());
    Snippet snippet = recordManager.createSnippet("a", testContent, user);

    String snippetContent =
        snippetController.getSnippetContent(snippet.getId(), principalTestUserStub, response);

    assertEquals(testContent, snippetContent);
  }

  @Test(expected = ObjectRetrievalFailureException.class)
  public void testExceptionOnGetttingUnexistingSnippetContent() {
    snippetController.getSnippetContent(UNEXISTING_SNIPPET_ID, principalTestUserStub, response);
  }
}
