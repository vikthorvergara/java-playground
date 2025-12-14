package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.extension.ExtensionContext;

@DisplayName("Advanced Parameterized Tests")
class CsvFileSourcePOCTest {

  private final CsvFileSourcePOC poc = new CsvFileSourcePOC();

  @ParameterizedTest
  @CsvSource({"10, 5, 2.0", "20, 4, 5.0", "100, 10, 10.0"})
  @DisplayName("CSV: Test division with custom delimiter")
  void testDivision(double a, double b, double expected) {
    assertEquals(expected, poc.divide(a, b), 0.001);
  }

  @ParameterizedTest
  @CsvSource(value = {"hello|HELLO", "world|WORLD", "test|TEST"}, delimiter = '|')
  @DisplayName("CSV: Test upper case with pipe delimiter")
  void testUpperCase(String input, String expected) {
    assertEquals(expected, poc.toUpperCase(input));
  }

  @ParameterizedTest
  @CsvSource({"100, 10, 90", "200, 20, 160", "1000, 50, 500"})
  @DisplayName("CSV: Test discount calculation")
  void testDiscount(int price, int discount, int expected) {
    assertEquals(expected, poc.calculateDiscount(price, discount));
  }

  @ParameterizedTest
  @ArgumentsSource(CustomArgumentsProvider.class)
  @DisplayName("ArgumentsSource: Test with custom provider")
  void testWithCustomProvider(String text, boolean expectedPalindrome) {
    assertEquals(expectedPalindrome, poc.isPalindrome(text));
  }

  static class CustomArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("radar", true),
          Arguments.of("hello", false),
          Arguments.of("level", true),
          Arguments.of("world", false),
          Arguments.of("civic", true));
    }
  }

  @ParameterizedTest
  @CsvSource({"true, TRUE", "false, FALSE", "yes, YES"})
  @DisplayName("Converter: Test with custom string converter")
  void testWithConverter(@ConvertWith(UpperCaseConverter.class) String converted, String expected) {
    assertEquals(expected, converted);
  }

  static class UpperCaseConverter extends SimpleArgumentConverter {
    @Override
    protected Object convert(Object source, Class<?> targetType) {
      if (source instanceof String) {
        return ((String) source).toUpperCase();
      }
      throw new IllegalArgumentException("Conversion not supported");
    }
  }

  @ParameterizedTest
  @CsvSource({"5, 10, 15", "3, 7, 10", "100, 200, 300"})
  @DisplayName("Aggregator: Test with custom aggregator")
  void testWithAggregator(@AggregateWith(SumAggregator.class) SumData sumData) {
    assertEquals(sumData.expected, poc.add(sumData.a, sumData.b));
  }

  static class SumData {
    int a;
    int b;
    int expected;

    SumData(int a, int b, int expected) {
      this.a = a;
      this.b = b;
      this.expected = expected;
    }
  }

  static class SumAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(ArgumentsAccessor accessor, ExtensionContext context)
        throws ArgumentsAggregationException {
      return new SumData(accessor.getInteger(0), accessor.getInteger(1), accessor.getInteger(2));
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ComplexDataProvider.class)
  @DisplayName("ArgumentsSource: Test with complex data provider")
  void testWithComplexProvider(int a, int b, int expectedSum, int expectedProduct) {
    assertEquals(expectedSum, poc.add(a, b));
    assertEquals(expectedProduct, poc.multiply(a, b));
  }

  static class ComplexDataProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(2, 3, 5, 6),
          Arguments.of(4, 5, 9, 20),
          Arguments.of(10, 10, 20, 100));
    }
  }

  @ParameterizedTest
  @CsvSource({"'hello', 5", "'', 0", "'world', 5"})
  @DisplayName("CSV: Test string length with quotes")
  void testStringLength(String text, int expected) {
    assertEquals(expected, poc.getStringLength(text));
  }

  @ParameterizedTest
  @CsvSource(nullValues = "NULL", value = {"NULL, 0", "test, 4", "hello, 5"})
  @DisplayName("CSV: Test with null values")
  void testWithNull(String text, int expectedLength) {
    assertEquals(expectedLength, poc.getStringLength(text));
  }

  @ParameterizedTest
  @ArgumentsSource(StringConcatProvider.class)
  @DisplayName("ArgumentsSource: Test concatenation")
  void testConcatenation(String a, String b, String expected) {
    assertEquals(expected, poc.concatenate(a, b));
  }

  static class StringConcatProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("Hello", "World", "Hello World"),
          Arguments.of("JUnit", "5", "JUnit 5"),
          Arguments.of("Test", "Driven", "Test Driven"));
    }
  }

  @ParameterizedTest
  @CsvSource({"2, 3", "5, 5", "10, 20"})
  @DisplayName("Accessor: Test with ArgumentsAccessor")
  void testWithAccessor(ArgumentsAccessor accessor) {
    int a = accessor.getInteger(0);
    int b = accessor.getInteger(1);

    int sum = poc.add(a, b);
    int product = poc.multiply(a, b);

    assertTrue(sum > 0);
    assertTrue(product > 0);
  }
}
