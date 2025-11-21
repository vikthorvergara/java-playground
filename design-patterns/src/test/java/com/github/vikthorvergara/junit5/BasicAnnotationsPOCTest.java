package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Basic JUnit Annotations")
class BasicAnnotationsPOCTest {

  private final BasicAnnotationsPOC calculator = new BasicAnnotationsPOC();

  @Test
  void testAddition() {
    assertEquals(5, calculator.add(2, 3));
  }

  @Test
  @DisplayName("Subtraction should return correct difference")
  void testSubtraction() {
    assertEquals(3, calculator.subtract(5, 2));
  }

  @Test
  @DisplayName("Multiplication: 4 x 3 = 12")
  void testMultiplication() {
    assertEquals(12, calculator.multiply(4, 3));
  }

  @Test
  @DisplayName("Division should handle normal cases")
  void testDivision() {
    assertEquals(5.0, calculator.divide(10, 2));
  }

  @Test
  @DisplayName("Division by zero should throw ArithmeticException")
  void testDivisionByZero() {
    assertThrows(ArithmeticException.class, () -> calculator.divide(10, 0));
  }

  @Test
  void testIsEvenWithEvenNumber() {
    assertTrue(calculator.isEven(4));
  }

  @Test
  void testIsEvenWithOddNumber() {
    assertFalse(calculator.isEven(7));
  }

  @Test
  @DisplayName("Concatenate two strings")
  void testConcatenation() {
    assertEquals("HelloWorld", calculator.concatenate("Hello", "World"));
  }

  @Test
  @DisplayName("Concatenate with null should return null")
  void testConcatenationWithNull() {
    assertNull(calculator.concatenate(null, "World"));
    assertNull(calculator.concatenate("Hello", null));
  }

  @Test
  @Disabled("This test is disabled for demonstration purposes")
  void testDisabledFeature() {
    fail("This test should not run");
  }
}
