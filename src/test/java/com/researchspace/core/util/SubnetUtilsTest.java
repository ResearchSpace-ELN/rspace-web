package com.researchspace.core.util;

import static org.junit.jupiter.api.Assertions.*;

import com.researchspace.core.util.SubnetUtils.SubnetInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Usage testing; src code is taken from Apache commons net project */
public class SubnetUtilsTest {

  private static final String VALID_IP = "123.222.111.192";
  private static final String VALID_CIDR = "123.222.111.192/27";

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void test() {
    String validCidr = VALID_CIDR;
    SubnetUtils utils = new SubnetUtils(validCidr);
    utils.setInclusiveHostCount(true);
    SubnetInfo info = utils.getInfo();
    assertEquals(VALID_IP, info.getLowAddress());
    assertEquals("123.222.111.223", info.getHighAddress());
    System.err.println(info.getNetworkAddress());
  }

  @Test
  public void testInvalid() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new SubnetUtils(VALID_IP); // must be cidr
        }); // must be cidr
  }

  @Test
  public void testIsIp4OrCidr() {
    assertTrue(SubnetUtils.isValidIp4OrCIDRAddress(VALID_CIDR));
    assertTrue(SubnetUtils.isValidIp4OrCIDRAddress(VALID_IP));
  }

  @Test
  public void testIsCidr() {
    assertTrue(SubnetUtils.isValidCIDRAddress(VALID_CIDR));
    assertFalse(SubnetUtils.isValidCIDRAddress(VALID_IP));
  }
}
