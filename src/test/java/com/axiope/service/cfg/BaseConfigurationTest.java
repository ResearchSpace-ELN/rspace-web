package com.axiope.service.cfg;

import static org.hamcrest.MatcherAssert.assertThat;

import com.researchspace.snapgene.wclient.SnapgeneWSClientImpl;
import com.researchspace.webapp.integrations.snapgene.SnapgeneDummy;
import com.researchspace.webapp.integrations.snapgene.SnapgeneWSNoop;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BaseConfigurationTest {

  @Test
  void noSnapGeneUrlFailsGracefullyWithNoopImpl() {
    BaseConfig cfg = new RSDevConfig();
    cfg.baseDocConverterConfig = new DocConverterBaseConfig();
    cfg.setSnapgeneUrl(null);
    Assertions.assertNotNull(cfg.snapgeneWSClient());
    assertThat(cfg.snapgeneWSClient(), Matchers.instanceOf(SnapgeneWSNoop.class));

    cfg.setSnapgeneUrl("http://some.valid.uri.com");
    Assertions.assertNotNull(cfg.snapgeneWSClient());
    assertThat(cfg.snapgeneWSClient(), Matchers.instanceOf(SnapgeneWSClientImpl.class));
  }

  @Test
  void noSnapGeneUrlFailsGracefullyWithDummyImplInRunProfile() {
    BaseConfig cfg = new TestAppConfig();
    cfg.baseDocConverterConfig = new DocConverterBaseConfig();
    cfg.setSnapgeneUrl(null);
    Assertions.assertNotNull(cfg.snapgeneWSClient());
    assertThat(cfg.snapgeneWSClient(), Matchers.instanceOf(SnapgeneDummy.class));

    cfg.setSnapgeneUrl("http://some.valid.uri.com");
    Assertions.assertNotNull(cfg.snapgeneWSClient());
    assertThat(cfg.snapgeneWSClient(), Matchers.instanceOf(SnapgeneWSClientImpl.class));
  }
}
