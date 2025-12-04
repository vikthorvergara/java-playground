package com.github.vikthorvergara.junit5;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@DisplayName("Timeout Tests")
class TimeoutsPOCTest {

  private final TimeoutsPOC poc = new TimeoutsPOC();

  @Test
  @Timeout(1)
  @DisplayName("Test completes within 1 second")
  void testWithTimeout() {
    assertEquals("fast", poc.fastOperation());
  }

  @Test
  @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
  @DisplayName("Test completes within 500 milliseconds")
  void testWithTimeoutMillis() {
    assertEquals("fast", poc.fastOperation());
  }

  @Test
  @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
  @DisplayName("Test with medium operation")
  void testMediumOperation() throws InterruptedException {
    assertEquals("medium", poc.mediumOperation());
  }

  @Test
  @DisplayName("assertTimeout: operation completes within time limit")
  void testAssertTimeout() {
    assertTimeout(
        ofSeconds(1),
        () -> {
          String result = poc.fastOperation();
          assertEquals("fast", result);
        });
  }

  @Test
  @DisplayName("assertTimeout with return value")
  void testAssertTimeoutWithReturn() {
    String result = assertTimeout(ofSeconds(1), () -> poc.fastOperation());
    assertEquals("fast", result);
  }

  @Test
  @DisplayName("assertTimeoutPreemptively: abort if exceeded")
  void testAssertTimeoutPreemptively() {
    assertTimeoutPreemptively(
        ofMillis(200),
        () -> {
          poc.mediumOperation();
        });
  }

  @Test
  @DisplayName("assertTimeout with computation")
  void testTimeoutWithComputation() throws InterruptedException {
    int result = assertTimeout(ofMillis(100), () -> poc.compute(5));
    assertEquals(25, result);
  }

  @Test
  @DisplayName("Multiple operations within timeout")
  void testMultipleOperationsWithTimeout() {
    assertTimeout(
        ofSeconds(1),
        () -> {
          assertEquals("fast", poc.fastOperation());
          assertEquals("fast", poc.fastOperation());
          assertEquals("fast", poc.fastOperation());
        });
  }

  @Test
  @DisplayName("Timeout with message")
  void testTimeoutWithMessage() {
    assertTimeout(
        ofSeconds(1),
        () -> poc.fastOperation(),
        "Operation should complete within 1 second");
  }

  @Test
  @DisplayName("Timeout with supplier message")
  void testTimeoutWithSupplierMessage() {
    assertTimeout(
        ofSeconds(1),
        () -> poc.fastOperation(),
        () -> "Operation took too long to complete");
  }

  @Test
  @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
  @DisplayName("Test delay within timeout")
  void testDelayWithinTimeout() throws InterruptedException {
    poc.processWithDelay(50);
    assertTrue(true);
  }

  @Test
  @DisplayName("assertTimeoutPreemptively stops immediately on timeout")
  void testPreemptiveTimeout() {
    assertTimeoutPreemptively(
        ofMillis(200),
        () -> {
          poc.processWithDelay(100);
          System.out.println("Completed within timeout");
        });
  }
}
