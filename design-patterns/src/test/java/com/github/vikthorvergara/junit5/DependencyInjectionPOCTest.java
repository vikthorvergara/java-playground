package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

@DisplayName("Dependency Injection in Tests")
class DependencyInjectionPOCTest {

  private final DependencyInjectionPOC poc = new DependencyInjectionPOC();

  @Test
  @DisplayName("TestInfo provides test metadata")
  void testWithTestInfo(TestInfo testInfo) {
    System.out.println("Display Name: " + testInfo.getDisplayName());
    System.out.println("Test Class: " + testInfo.getTestClass().orElse(null));
    System.out.println("Test Method: " + testInfo.getTestMethod().orElse(null));
    System.out.println("Tags: " + testInfo.getTags());

    assertEquals("TestInfo provides test metadata", testInfo.getDisplayName());
    assertTrue(testInfo.getTestClass().isPresent());
    assertTrue(testInfo.getTestMethod().isPresent());
  }

  @Test
  @Tag("unit")
  @Tag("fast")
  @DisplayName("TestInfo with tags")
  void testWithTags(TestInfo testInfo) {
    System.out.println("Tags: " + testInfo.getTags());
    assertTrue(testInfo.getTags().contains("unit"));
    assertTrue(testInfo.getTags().contains("fast"));
  }

  @Test
  @DisplayName("TestReporter for publishing data")
  void testWithTestReporter(TestReporter testReporter) {
    testReporter.publishEntry("key1", "value1");
    testReporter.publishEntry("key2", "value2");
    testReporter.publishEntry("status", "running");

    String result = poc.processData("test-data");
    testReporter.publishEntry("result", result);

    assertTrue(result.contains("test-data"));
  }

  @RepeatedTest(3)
  @DisplayName("RepetitionInfo in repeated tests")
  void testWithRepetitionInfo(RepetitionInfo repetitionInfo, TestInfo testInfo) {
    System.out.println(
        "Repetition "
            + repetitionInfo.getCurrentRepetition()
            + " of "
            + repetitionInfo.getTotalRepetitions());
    System.out.println("Test Display Name: " + testInfo.getDisplayName());

    assertTrue(repetitionInfo.getCurrentRepetition() <= repetitionInfo.getTotalRepetitions());
    assertEquals(3, repetitionInfo.getTotalRepetitions());
  }

  @Test
  @DisplayName("Multiple injected parameters")
  void testWithMultipleParameters(TestInfo testInfo, TestReporter testReporter) {
    testReporter.publishEntry("testName", testInfo.getDisplayName());
    testReporter.publishEntry("testClass", testInfo.getTestClass().get().getName());

    String result = poc.processData("input");
    testReporter.publishEntry("result", result);

    assertEquals("Processed: input", result);
  }

  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    System.out.println("Before test: " + testInfo.getDisplayName());
  }

  @AfterEach
  void afterEach(TestInfo testInfo, TestReporter testReporter) {
    System.out.println("After test: " + testInfo.getDisplayName());
    testReporter.publishEntry("testCompleted", testInfo.getDisplayName());
  }

  @Test
  @DisplayName("TestInfo in nested test")
  void testNestedInfo(TestInfo testInfo) {
    System.out.println("Method name: " + testInfo.getTestMethod().get().getName());
    assertEquals("testNestedInfo", testInfo.getTestMethod().get().getName());
  }

  @RepeatedTest(value = 5, name = "Iteration {currentRepetition} of {totalRepetitions}")
  @DisplayName("RepetitionInfo with TestReporter")
  void testRepetitionWithReporter(RepetitionInfo repetitionInfo, TestReporter testReporter) {
    int current = repetitionInfo.getCurrentRepetition();
    testReporter.publishEntry("iteration", String.valueOf(current));
    testReporter.publishEntry("result", String.valueOf(poc.calculate(current, 10)));

    assertTrue(current >= 1 && current <= 5);
  }

  @Test
  @DisplayName("Validate with all parameters")
  void testValidateWithAllParams(TestInfo testInfo, TestReporter testReporter) {
    String testName = testInfo.getDisplayName();
    testReporter.publishEntry("validating", testName);

    boolean result = poc.validate("test-input");
    testReporter.publishEntry("validationResult", String.valueOf(result));

    assertTrue(result);
  }
}
