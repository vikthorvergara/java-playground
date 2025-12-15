package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@DisplayName("Test Execution Listener Tests")
@ExtendWith(TestExecutionListenerPOCTest.LoggingExtension.class)
class TestExecutionListenerPOCTest {

  private TestExecutionListenerPOC poc;

  @BeforeAll
  static void setupAll() {
    System.out.println("@BeforeAll: Setting up test class");
  }

  @BeforeEach
  void setUp() {
    System.out.println("@BeforeEach: Creating new POC instance");
    poc = new TestExecutionListenerPOC();
  }

  @Test
  @DisplayName("Test increment method")
  void testIncrement() {
    System.out.println("@Test: Testing increment");
    assertEquals(1, poc.increment());
    assertEquals(2, poc.increment());
  }

  @Test
  @DisplayName("Test counter getter")
  void testGetCounter() {
    System.out.println("@Test: Testing get counter");
    assertEquals(0, poc.getCounter());
    poc.increment();
    assertEquals(1, poc.getCounter());
  }

  @Test
  @DisplayName("Test double value")
  void testDoubleValue() {
    System.out.println("@Test: Testing double value");
    assertEquals(10, poc.doubleValue(5));
    assertEquals(20, poc.doubleValue(10));
  }

  @Test
  @DisplayName("Test positive check")
  void testIsPositive() {
    System.out.println("@Test: Testing is positive");
    assertTrue(poc.isPositive(5));
    assertFalse(poc.isPositive(-5));
  }

  @Test
  @DisplayName("Test message processing")
  void testProcessMessage() {
    System.out.println("@Test: Testing process message");
    assertEquals("Processed: Hello", poc.processMessage("Hello"));
  }

  @AfterEach
  void tearDown() {
    System.out.println("@AfterEach: Cleaning up POC instance");
    poc = null;
  }

  @AfterAll
  static void tearDownAll() {
    System.out.println("@AfterAll: Tearing down test class");
  }

  static class LoggingExtension
      implements BeforeAllCallback,
          BeforeEachCallback,
          BeforeTestExecutionCallback,
          AfterTestExecutionCallback,
          AfterEachCallback,
          AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
      System.out.println(
          "[Extension] beforeAll: Starting test class - " + context.getDisplayName());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
      System.out.println("[Extension] beforeEach: Preparing test - " + context.getDisplayName());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
      System.out.println(
          "[Extension] beforeTestExecution: About to execute - " + context.getDisplayName());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
      System.out.println(
          "[Extension] afterTestExecution: Test completed - " + context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) {
      System.out.println("[Extension] afterEach: Cleaning up test - " + context.getDisplayName());
    }

    @Override
    public void afterAll(ExtensionContext context) {
      System.out.println(
          "[Extension] afterAll: All tests completed - " + context.getDisplayName());
    }
  }
}
