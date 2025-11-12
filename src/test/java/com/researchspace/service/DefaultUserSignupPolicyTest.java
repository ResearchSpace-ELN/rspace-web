package com.researchspace.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.researchspace.model.record.TestFactory;
import com.researchspace.properties.IMutablePropertyHolder;
import com.researchspace.properties.PropertyHolder;
import com.researchspace.service.impl.DefaultUserSignupPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class DefaultUserSignupPolicyTest {
  @Mock UserManager mgr;

  private IMutablePropertyHolder props;
  private DefaultUserSignupPolicy defaultImpl;

  @BeforeEach
  public void setUp() {
    defaultImpl = new DefaultUserSignupPolicy();
    props = new PropertyHolder();
    defaultImpl.setProperties(props);
    defaultImpl.setUserManager(mgr);
  }

  @Test
  public void testSaveUserThrowsISEIfNotConfiguredForCloud() throws UserExistsException {
    assertThrows(
        IllegalStateException.class,
        () -> {
          props.setCloud("true");
          defaultImpl.saveUser(TestFactory.createAnyUser("any"), null);
        });
  }
}
