package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JUnit Assumptions")
class AssumptionsPOCTest {

  private final AssumptionsPOC poc = new AssumptionsPOC();

  @Test
  @DisplayName("assumeTrue: run test only if condition is true")
  void testAssumeTrue() {
    assumeTrue(poc.getEnvironment().equals("development"));

    System.out.println("This test runs only in development environment");
    assertNotNull(poc.getOperatingSystem());
  }

  @Test
  @DisplayName("assumeFalse: run test only if condition is false")
  void testAssumeFalse() {
    assumeFalse(poc.isContinuousIntegration());

    System.out.println("This test runs only when NOT in CI");
    assertNotNull(poc.getJavaVersion());
  }

  @Test
  @DisplayName("assumeTrue with message: custom abort message")
  void testAssumeTrueWithMessage() {
    assumeTrue(
        !poc.getOperatingSystem().toLowerCase().contains("windows"),
        "Test disabled on Windows");

    System.out.println("This test doesn't run on Windows");
    assertTrue(true);
  }

  @Test
  @DisplayName("assumingThat: execute code block conditionally")
  void testAssumingThat() {
    assumingThat(
        poc.getEnvironment().equals("development"),
        () -> {
          System.out.println("Running development-specific assertions");
          assertEquals("development", poc.getEnvironment());
        });

    System.out.println("This always executes regardless of assumption");
    assertNotNull(poc.getOperatingSystem());
  }

  @Test
  @DisplayName("assumingThat: multiple scenarios")
  void testMultipleAssumingThat() {
    assumingThat(
        poc.isContinuousIntegration(),
        () -> {
          System.out.println("Running in CI - skipping expensive operations");
        });

    assumingThat(
        !poc.isContinuousIntegration(),
        () -> {
          System.out.println("Running locally - performing expensive calculation");
          int result = poc.performExpensiveCalculation();
          assertEquals(499500, result);
        });

    System.out.println("Common assertion that always runs");
    assertNotNull(poc.getJavaVersion());
  }

  @Test
  @DisplayName("Assumptions with database availability")
  void testDatabaseAssumption() {
    assumeTrue(poc.isDatabaseAvailable(), "Database not available - skipping test");

    System.out.println("This test would run database operations");
    assertTrue(true);
  }

  @Test
  @DisplayName("Assumption with supplier for lazy message evaluation")
  void testAssumptionWithSupplier() {
    assumeTrue(
        poc.getEnvironment().equals("development"),
        () -> "Test requires development environment but found: " + poc.getEnvironment());

    System.out.println("Running in development");
    assertNotNull(poc.getOperatingSystem());
  }

  @Test
  @DisplayName("Combined assumptions and assertions")
  void testCombinedAssumptionsAndAssertions() {
    assumeFalse(poc.isContinuousIntegration());

    String data = poc.fetchFromExternalService();

    assumingThat(
        data != null,
        () -> {
          System.out.println("Data available: " + data);
          assertEquals("external-data", data);
        });

    assertNotNull(poc.getOperatingSystem());
  }
}
