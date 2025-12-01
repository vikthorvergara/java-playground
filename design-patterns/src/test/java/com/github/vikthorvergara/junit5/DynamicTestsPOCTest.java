package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

@DisplayName("Dynamic Tests")
class DynamicTestsPOCTest {

  private final DynamicTestsPOC poc = new DynamicTestsPOC();

  @TestFactory
  @DisplayName("Generate prime number tests dynamically")
  Stream<DynamicTest> testPrimeNumbers() {
    int[] primes = {2, 3, 5, 7, 11, 13, 17, 19};

    return Arrays.stream(primes)
        .mapToObj(
            number ->
                dynamicTest(
                    "Testing if " + number + " is prime",
                    () -> assertTrue(poc.isPrime(number))));
  }

  @TestFactory
  @DisplayName("Generate non-prime number tests dynamically")
  List<DynamicTest> testNonPrimeNumbers() {
    return Arrays.asList(
        dynamicTest("4 is not prime", () -> assertFalse(poc.isPrime(4))),
        dynamicTest("6 is not prime", () -> assertFalse(poc.isPrime(6))),
        dynamicTest("8 is not prime", () -> assertFalse(poc.isPrime(8))),
        dynamicTest("9 is not prime", () -> assertFalse(poc.isPrime(9))));
  }

  @TestFactory
  @DisplayName("Generate Fibonacci tests dynamically")
  Stream<DynamicTest> testFibonacci() {
    int[][] testCases = {{0, 0}, {1, 1}, {2, 1}, {3, 2}, {4, 3}, {5, 5}, {6, 8}};

    return Arrays.stream(testCases)
        .map(
            testCase ->
                dynamicTest(
                    "Fibonacci(" + testCase[0] + ") = " + testCase[1],
                    () -> assertEquals(testCase[1], poc.fibonacci(testCase[0]))));
  }

  @TestFactory
  @DisplayName("Generate factorial tests dynamically")
  Stream<DynamicTest> testFactorial() {
    return IntStream.rangeClosed(0, 5)
        .mapToObj(
            n -> {
              int expected = calculateExpectedFactorial(n);
              return dynamicTest(
                  "Factorial of " + n + " equals " + expected,
                  () -> assertEquals(expected, poc.factorial(n)));
            });
  }

  private int calculateExpectedFactorial(int n) {
    int result = 1;
    for (int i = 2; i <= n; i++) {
      result *= i;
    }
    return result;
  }

  @TestFactory
  @DisplayName("Generate string reverse tests dynamically")
  Stream<DynamicTest> testStringReverse() {
    String[][] testCases = {
      {"hello", "olleh"}, {"world", "dlrow"}, {"junit", "tinuj"}, {"test", "tset"}
    };

    return Arrays.stream(testCases)
        .map(
            testCase ->
                dynamicTest(
                    "Reverse of '" + testCase[0] + "' is '" + testCase[1] + "'",
                    () -> assertEquals(testCase[1], poc.reverseString(testCase[0]))));
  }

  @TestFactory
  @DisplayName("Generate range tests dynamically")
  Stream<DynamicTest> testRange() {
    return IntStream.of(5, 10, 15)
        .mapToObj(
            end ->
                dynamicTest(
                    "Range from 1 to " + end,
                    () -> {
                      List<Integer> range = poc.getRange(1, end);
                      assertEquals(end, range.size());
                      assertEquals(1, range.get(0));
                      assertEquals(end, range.get(end - 1));
                    }));
  }

  @TestFactory
  @DisplayName("Generate sum of digits tests dynamically")
  Stream<DynamicTest> testSumDigits() {
    int[][] testCases = {{123, 6}, {456, 15}, {789, 24}, {1000, 1}};

    return Arrays.stream(testCases)
        .map(
            testCase ->
                dynamicTest(
                    "Sum of digits of " + testCase[0] + " equals " + testCase[1],
                    () -> assertEquals(testCase[1], poc.sumDigits(testCase[0]))));
  }

  @TestFactory
  @DisplayName("Generate nested dynamic tests")
  Stream<DynamicTest> testNestedDynamic() {
    return Stream.of(
        dynamicTest(
            "Group 1: Small primes",
            () -> {
              assertTrue(poc.isPrime(2));
              assertTrue(poc.isPrime(3));
            }),
        dynamicTest(
            "Group 2: Medium primes",
            () -> {
              assertTrue(poc.isPrime(7));
              assertTrue(poc.isPrime(11));
            }),
        dynamicTest(
            "Group 3: Larger primes",
            () -> {
              assertTrue(poc.isPrime(13));
              assertTrue(poc.isPrime(17));
            }));
  }
}
