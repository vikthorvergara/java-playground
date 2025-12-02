package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;

@DisplayName("Conditional Test Execution")
class ConditionalExecutionPOCTest {

  private final ConditionalExecutionPOC poc = new ConditionalExecutionPOC();

  @Test
  @EnabledOnOs(OS.LINUX)
  @DisplayName("Run only on Linux")
  void testOnLinux() {
    System.out.println("Running on Linux");
    assertTrue(poc.getOSName().toLowerCase().contains("linux"));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  @DisplayName("Run only on Windows")
  void testOnWindows() {
    System.out.println("Running on Windows");
    assertTrue(poc.getOSName().toLowerCase().contains("windows"));
  }

  @Test
  @EnabledOnOs(OS.MAC)
  @DisplayName("Run only on Mac")
  void testOnMac() {
    System.out.println("Running on Mac");
    assertTrue(poc.getOSName().toLowerCase().contains("mac"));
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  @DisplayName("Disabled on Windows")
  void testDisabledOnWindows() {
    System.out.println("Not running on Windows");
    assertNotNull(poc.getOSName());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC})
  @DisplayName("Run on Linux or Mac")
  void testOnLinuxOrMac() {
    System.out.println("Running on Linux or Mac");
    assertNotNull(poc.getOSName());
  }

  @Test
  @EnabledOnJre(JRE.JAVA_21)
  @DisplayName("Run only on Java 21")
  void testOnJava21() {
    System.out.println("Running on Java 21");
    assertTrue(poc.getJavaVersion().startsWith("21"));
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  @DisplayName("Disabled on Java 8")
  void testDisabledOnJava8() {
    System.out.println("Not running on Java 8");
    assertNotNull(poc.getJavaVersion());
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_11)
  @DisplayName("Run on Java 11 or higher")
  void testOnJava11OrHigher() {
    System.out.println("Running on Java 11+");
    assertTrue(poc.isJavaVersionSupported());
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_11, max = JRE.JAVA_21)
  @DisplayName("Run on Java 11 to 21")
  void testOnJava11To21() {
    System.out.println("Running on Java 11-21");
    assertTrue(poc.isJavaVersionSupported());
  }

  @Test
  @EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
  @DisplayName("Run on 64-bit architecture")
  void testOn64BitArchitecture() {
    System.out.println("Running on 64-bit");
    assertTrue(poc.getArchitecture().contains("64"));
  }

  @Test
  @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
  @DisplayName("Disabled on 32-bit architecture")
  void testDisabledOn32Bit() {
    System.out.println("Not running on 32-bit");
    assertNotNull(poc.getArchitecture());
  }

  @Test
  @EnabledIfEnvironmentVariable(named = "CI", matches = "true")
  @DisplayName("Run only in CI environment")
  void testInCI() {
    System.out.println("Running in CI");
    assertNotNull(System.getenv("CI"));
  }

  @Test
  @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
  @DisplayName("Disabled in CI environment")
  void testNotInCI() {
    System.out.println("Not running in CI");
    assertNotNull(poc.getOSName());
  }

  @Test
  @EnabledIf("customCondition")
  @DisplayName("Run if custom condition is true")
  void testWithCustomCondition() {
    System.out.println("Running with custom condition");
    assertTrue(true);
  }

  boolean customCondition() {
    return true;
  }

  @Test
  @DisabledIf("customDisabledCondition")
  @DisplayName("Disabled if custom condition is true")
  void testDisabledWithCustomCondition() {
    System.out.println("This should not run if condition is true");
    assertTrue(true);
  }

  boolean customDisabledCondition() {
    return false;
  }

  @Test
  @DisplayName("Always runs - no conditions")
  void testAlwaysRuns() {
    System.out.println("This test always runs");
    assertNotNull(poc.getOSName());
    assertNotNull(poc.getJavaVersion());
  }
}
