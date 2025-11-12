package com.researchspace.api.v1.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.api.v1.controller.ApiAccountInitialiser;
import com.researchspace.core.testutil.CoreTestUtils;
import com.researchspace.model.User;
import com.researchspace.model.record.TestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ApiProdConfigTest {

  @InjectMocks ProdAPIConfig cfg;

  @Test
  public void accountInitialisorRequiresApiBetaEnabled() throws Exception {
    User anyUser = TestFactory.createAnyUser("any");
    cfg.setBetaApiEnabled(Boolean.FALSE);
    ApiAccountInitialiser initialiser = cfg.accountInitialiser();

    CoreTestUtils.assertExceptionThrown(
        () -> initialiser.initialiseUser(anyUser), UnsupportedOperationException.class);

    cfg.setBetaApiEnabled(Boolean.TRUE);
    ApiAccountInitialiser realInitialiser = cfg.accountInitialiser();
    assertTrue(realInitialiser instanceof AccountInitialiserImpl);
  }
}
