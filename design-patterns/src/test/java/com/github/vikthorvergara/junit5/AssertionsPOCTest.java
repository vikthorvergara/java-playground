package com.github.vikthorvergara.junit5;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JUnit Assertions")
class AssertionsPOCTest {

  private final AssertionsPOC poc = new AssertionsPOC();

  @Test
  @DisplayName("assertEquals: basic equality")
  void testAssertEquals() {
    assertEquals(25, poc.square(5));
    assertEquals(15, poc.sumArray(new int[] {1, 2, 3, 4, 5}));
  }

  @Test
  @DisplayName("assertNotEquals: values are different")
  void testAssertNotEquals() {
    assertNotEquals(20, poc.square(5));
  }

  @Test
  @DisplayName("assertTrue and assertFalse")
  void testTrueAndFalse() {
    assertTrue(poc.isPositive(10));
    assertFalse(poc.isPositive(-5));
  }

  @Test
  @DisplayName("assertNull and assertNotNull")
  void testNullChecks() {
    assertNull(poc.findUserById(999));
    assertNotNull(poc.findUserById(1));
  }

  @Test
  @DisplayName("assertSame and assertNotSame: object identity")
  void testObjectIdentity() {
    AssertionsPOC.User user1 = poc.findUserById(1);
    AssertionsPOC.User user2 = poc.findUserById(1);
    AssertionsPOC.User user3 = user1;

    assertNotSame(user1, user2);
    assertSame(user1, user3);
  }

  @Test
  @DisplayName("assertArrayEquals: array content equality")
  void testArrayEquals() {
    int[] expected = {1, 2, 3};
    int[] actual = {1, 2, 3};
    assertArrayEquals(expected, actual);
  }

  @Test
  @DisplayName("assertIterableEquals: collection equality")
  void testIterableEquals() {
    List<String> expected = List.of("A", "B", "C");
    List<String> actual = poc.getElements();
    assertIterableEquals(expected, actual);
  }

  @Test
  @DisplayName("assertThrows: exception handling")
  void testAssertThrows() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> poc.processValue(-1));
    assertEquals("Value must be non-negative", exception.getMessage());
  }

  @Test
  @DisplayName("assertDoesNotThrow: no exception expected")
  void testAssertDoesNotThrow() {
    assertDoesNotThrow(() -> poc.processValue(10));
  }

  @Test
  @DisplayName("assertAll: grouped assertions")
  void testAssertAll() {
    assertAll(
        "Grouped assertions",
        () -> assertEquals(25, poc.square(5)),
        () -> assertTrue(poc.isPositive(10)),
        () -> assertNotNull(poc.findUserById(1)),
        () -> assertEquals(15, poc.sumArray(new int[] {1, 2, 3, 4, 5})));
  }

  @Test
  @DisplayName("assertTimeout: operation completes within time limit")
  void testAssertTimeout() {
    assertTimeout(ofSeconds(1), () -> poc.slowOperation());
  }

  @Test
  @DisplayName("assertTimeoutPreemptively: abort if timeout exceeded")
  void testAssertTimeoutPreemptively() {
    assertTimeoutPreemptively(ofSeconds(1), () -> poc.slowOperation());
  }

  @Test
  @DisplayName("Custom assertion message")
  void testCustomMessage() {
    assertEquals(25, poc.square(5), "Square of 5 should be 25");
  }

  @Test
  @DisplayName("Lazy assertion message with supplier")
  void testLazyMessage() {
    assertTrue(poc.isPositive(10), () -> "Expected positive number but got negative");
  }

  @Test
  @DisplayName("assertLinesMatch: string list pattern matching")
  void testAssertLinesMatch() {
    List<String> expected = List.of("A", "B", "C");
    List<String> actual = poc.getElements();
    assertLinesMatch(expected, actual);
  }
}
