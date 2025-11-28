package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@DisplayName("Parameterized Tests")
class ParameterizedTestsPOCTest {

  private final ParameterizedTestsPOC poc = new ParameterizedTestsPOC();

  @ParameterizedTest
  @ValueSource(strings = {"radar", "level", "civic", "madam"})
  @DisplayName("ValueSource: Test palindromes")
  void testPalindromes(String word) {
    assertTrue(poc.isPalindrome(word));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 4, 6, 8, 10})
  @DisplayName("ValueSource: Test even numbers")
  void testEvenNumbers(int number) {
    assertTrue(poc.isEven(number));
  }

  @ParameterizedTest
  @ValueSource(strings = {"hello", "world", "junit", "test"})
  @DisplayName("ValueSource: Test string lengths")
  void testStringLength(String text) {
    assertTrue(poc.length(text) > 0);
  }

  @ParameterizedTest
  @CsvSource({"1, 1", "2, 4", "3, 9", "4, 16", "5, 25"})
  @DisplayName("CsvSource: Test squares")
  void testSquares(int input, int expected) {
    assertEquals(expected, poc.square(input));
  }

  @ParameterizedTest
  @CsvSource({"hello, HELLO", "world, WORLD", "junit, JUNIT"})
  @DisplayName("CsvSource: Test upper case conversion")
  void testToUpperCase(String input, String expected) {
    assertEquals(expected, poc.toUpperCase(input));
  }

  @ParameterizedTest
  @CsvSource({"2, 3, 5", "10, 20, 30", "100, 200, 300"})
  @DisplayName("CsvSource: Test addition")
  void testAddition(int a, int b, int expected) {
    assertEquals(expected, poc.add(a, b));
  }

  @ParameterizedTest
  @MethodSource("provideStringsForPalindrome")
  @DisplayName("MethodSource: Test palindrome detection")
  void testPalindromeWithMethodSource(String word, boolean expected) {
    assertEquals(expected, poc.isPalindrome(word));
  }

  static Stream<Arguments> provideStringsForPalindrome() {
    return Stream.of(
        Arguments.of("radar", true),
        Arguments.of("hello", false),
        Arguments.of("level", true),
        Arguments.of("world", false));
  }

  @ParameterizedTest
  @MethodSource("provideNumbersForDivision")
  @DisplayName("MethodSource: Test division")
  void testDivision(double a, double b, double expected) {
    assertEquals(expected, poc.divide(a, b), 0.001);
  }

  static Stream<Arguments> provideNumbersForDivision() {
    return Stream.of(
        Arguments.of(10.0, 2.0, 5.0),
        Arguments.of(20.0, 4.0, 5.0),
        Arguments.of(100.0, 10.0, 10.0));
  }

  @ParameterizedTest
  @EnumSource(ParameterizedTestsPOC.DayOfWeek.class)
  @DisplayName("EnumSource: Test all days of week")
  void testAllDaysOfWeek(ParameterizedTestsPOC.DayOfWeek day) {
    assertNotNull(day);
  }

  @ParameterizedTest
  @EnumSource(
      value = ParameterizedTestsPOC.DayOfWeek.class,
      names = {"SATURDAY", "SUNDAY"})
  @DisplayName("EnumSource: Test weekend days")
  void testWeekendDays(ParameterizedTestsPOC.DayOfWeek day) {
    assertTrue(poc.isWeekend(day));
  }

  @ParameterizedTest
  @EnumSource(
      value = ParameterizedTestsPOC.DayOfWeek.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"SATURDAY", "SUNDAY"})
  @DisplayName("EnumSource: Test weekday days")
  void testWeekdayDays(ParameterizedTestsPOC.DayOfWeek day) {
    assertTrue(poc.isWeekday(day));
  }

  @ParameterizedTest
  @MethodSource("providePersons")
  @DisplayName("MethodSource: Test with custom objects")
  void testPersonIsAdult(ParameterizedTestsPOC.Person person, boolean expectedAdult) {
    assertEquals(expectedAdult, person.isAdult());
  }

  static Stream<Arguments> providePersons() {
    return Stream.of(
        Arguments.of(new ParameterizedTestsPOC.Person("Vikthor", 30), true),
        Arguments.of(new ParameterizedTestsPOC.Person("Barney", 9), false));
  }

  @ParameterizedTest
  @CsvSource(delimiter = '|', value = {"hello|5", "world|5", "test|4"})
  @DisplayName("CsvSource: Test with custom delimiter")
  void testWithCustomDelimiter(String text, int expectedLength) {
    assertEquals(expectedLength, poc.length(text));
  }

  @ParameterizedTest
  @CsvSource(nullValues = "NULL", value = {"NULL", "hello", "world"})
  @DisplayName("CsvSource: Test with null values")
  void testWithNullValues(String text) {
    int length = poc.length(text);
    assertTrue(length >= 0);
  }

  @ParameterizedTest(name = "[{index}] Testing with input: {0}")
  @ValueSource(ints = {1, 2, 3, 4, 5})
  @DisplayName("Custom display name pattern")
  void testWithCustomDisplayName(int number) {
    assertTrue(poc.isPositive(number));
  }
}
