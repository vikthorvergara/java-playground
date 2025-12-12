package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

@DisplayName("Test Reporter Tests")
class TestReporterPOCTest {

  private final TestReporterPOC poc = new TestReporterPOC();

  @Test
  @DisplayName("Test with single report entry")
  void testWithSingleEntry(TestReporter testReporter) {
    testReporter.publishEntry("timestamp", String.valueOf(System.currentTimeMillis()));

    String result = poc.processUser("Alice", 30);
    assertEquals("User: Alice, Age: 30", result);

    testReporter.publishEntry("result", result);
  }

  @Test
  @DisplayName("Test with multiple report entries")
  void testWithMultipleEntries(TestReporter testReporter) {
    testReporter.publishEntry("testPhase", "setup");

    int a = 10;
    int b = 20;

    testReporter.publishEntry("operand_a", String.valueOf(a));
    testReporter.publishEntry("operand_b", String.valueOf(b));

    int result = poc.calculate(a, b);

    testReporter.publishEntry("result", String.valueOf(result));
    testReporter.publishEntry("testPhase", "complete");

    assertEquals(30, result);
  }

  @Test
  @DisplayName("Test with map-based report entries")
  void testWithMapEntries(TestReporter testReporter) {
    String username = "testuser";

    testReporter.publishEntry(
        Map.of(
            "action", "fetchUserData",
            "username", username,
            "timestamp", String.valueOf(System.currentTimeMillis())));

    Map<String, Object> userData = poc.getUserData(username);

    testReporter.publishEntry(
        Map.of(
            "userDataSize", String.valueOf(userData.size()),
            "username", userData.get("username").toString(),
            "active", userData.get("active").toString()));

    assertEquals(username, userData.get("username"));
    assertTrue((Boolean) userData.get("active"));
  }

  @Test
  @DisplayName("Test validation with reporter")
  void testValidation(TestReporter testReporter) {
    String[] inputs = {"ab", "abc", "abcd", "abcde"};

    for (String input : inputs) {
      boolean isValid = poc.validateInput(input);

      testReporter.publishEntry(
          Map.of(
              "input", input,
              "length", String.valueOf(input.length()),
              "valid", String.valueOf(isValid)));

      if (input.length() > 3) {
        assertTrue(isValid, "Input '" + input + "' should be valid");
      } else {
        assertFalse(isValid, "Input '" + input + "' should be invalid");
      }
    }
  }

  @Test
  @DisplayName("Test calculations with detailed reporting")
  void testCalculationsWithReporting(TestReporter testReporter) {
    double x = 10.0;
    double y = 5.0;
    String[] operations = {"add", "subtract", "multiply", "divide"};

    testReporter.publishEntry("testStart", "Beginning calculation tests");

    for (String operation : operations) {
      testReporter.publishEntry(
          Map.of("operation", operation, "x", String.valueOf(x), "y", String.valueOf(y)));

      double result = poc.performCalculation(x, y, operation);

      testReporter.publishEntry("result_" + operation, String.valueOf(result));

      switch (operation) {
        case "add":
          assertEquals(15.0, result, 0.001);
          break;
        case "subtract":
          assertEquals(5.0, result, 0.001);
          break;
        case "multiply":
          assertEquals(50.0, result, 0.001);
          break;
        case "divide":
          assertEquals(2.0, result, 0.001);
          break;
      }
    }

    testReporter.publishEntry("testEnd", "All calculations completed");
  }

  @Test
  @DisplayName("Test exception reporting")
  void testExceptionReporting(TestReporter testReporter) {
    testReporter.publishEntry("testScenario", "Division by zero");

    assertThrows(
        ArithmeticException.class,
        () -> {
          poc.performCalculation(10, 0, "divide");
        });

    testReporter.publishEntry("exceptionCaught", "ArithmeticException");
    testReporter.publishEntry("testResult", "PASS");
  }

  @Test
  @DisplayName("Test with nested reporting")
  void testNestedReporting(TestReporter testReporter) {
    testReporter.publishEntry("outerTest", "start");

    for (int i = 1; i <= 3; i++) {
      testReporter.publishEntry("iteration", String.valueOf(i));

      int result = poc.calculate(i, i * 10);

      testReporter.publishEntry(
          Map.of("iteration", String.valueOf(i), "result", String.valueOf(result)));

      assertTrue(result > 0);
    }

    testReporter.publishEntry("outerTest", "end");
  }

  @Test
  @DisplayName("Test with key-value report")
  void testKeyValueReport(TestReporter testReporter) {
    testReporter.publishEntry("key1", "value1");
    testReporter.publishEntry("key2", "value2");
    testReporter.publishEntry("key3", "value3");

    String result = poc.processUser("Bob", 25);

    testReporter.publishEntry("finalResult", result);

    assertNotNull(result);
  }
}
