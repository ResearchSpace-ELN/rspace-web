package com.axiope.model.record.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.researchspace.model.User;
import com.researchspace.model.inventory.Sample;
import com.researchspace.model.record.TestFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class SampleTemplateBuiltInTest {

  @Mock IBuiltInPersistor m_initializer;

  @InjectMocks AntibodySampleTemplate antibody;

  @Test
  public void test() {

    User anyUser = TestFactory.createAnyUser("any");
    Optional<Sample> opt = antibody.createSampleTemplate(anyUser);
    assertTrue(opt.isPresent());
    assertEquals(10, opt.get().getActiveFields().size());
  }
}
