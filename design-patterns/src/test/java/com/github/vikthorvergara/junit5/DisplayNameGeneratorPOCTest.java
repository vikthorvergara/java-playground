package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Display Name Generator Tests")
class DisplayNameGeneratorPOCTest {

  private final DisplayNameGeneratorPOC poc = new DisplayNameGeneratorPOC();

  @Nested
  @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
  @DisplayName("Replace Underscores Strategy")
  class ReplaceUnderscoresTests {

    @Test
    void test_addition_with_positive_numbers() {
      assertEquals(5, poc.add(2, 3));
    }

    @Test
    void test_multiplication_with_positive_numbers() {
      assertEquals(20, poc.multiply(4, 5));
    }

    @Test
    void test_validation_with_valid_input() {
      assertTrue(poc.isValid("test"));
    }
  }

  @Nested
  @DisplayNameGeneration(DisplayNameGenerator.IndicativeSentences.class)
  @DisplayName("Indicative Sentences Strategy")
  class IndicativeSentencesTests {

    @Test
    void testAddition() {
      assertEquals(7, poc.add(3, 4));
    }

    @Test
    void testMultiplication() {
      assertEquals(12, poc.multiply(3, 4));
    }

    @Test
    void testValidation() {
      assertFalse(poc.isValid(""));
    }
  }

  @Nested
  @DisplayNameGeneration(CustomDisplayNameGenerator.class)
  @DisplayName("Custom Display Name Strategy")
  class CustomDisplayNameTests {

    @Test
    void testAddMethod() {
      assertEquals(10, poc.add(5, 5));
    }

    @Test
    void testMultiplyMethod() {
      assertEquals(25, poc.multiply(5, 5));
    }

    @Test
    void testIsValidMethod() {
      assertTrue(poc.isValid("valid"));
    }
  }

  static class CustomDisplayNameGenerator implements DisplayNameGenerator {

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
      return "Custom: " + testClass.getSimpleName();
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
      return "Nested: " + nestedClass.getSimpleName();
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
      String methodName = testMethod.getName();
      return "Testing " + camelToWords(methodName);
    }

    private String camelToWords(String camelCase) {
      return camelCase.replaceAll("([A-Z])", " $1").trim().toLowerCase();
    }
  }

  @Nested
  @DisplayNameGeneration(DisplayNameGenerator.Simple.class)
  @DisplayName("Simple Strategy")
  class SimpleStrategyTests {

    @Test
    void simpleTest() {
      assertEquals(8, poc.add(3, 5));
    }

    @Test
    void anotherSimpleTest() {
      assertEquals(15, poc.multiply(3, 5));
    }
  }
}
