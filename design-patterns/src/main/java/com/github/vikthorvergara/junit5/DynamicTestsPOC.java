package com.github.vikthorvergara.junit5;

import java.util.ArrayList;
import java.util.List;

public class DynamicTestsPOC {

  public static void main(String[] args) {
    DynamicTestsPOC poc = new DynamicTestsPOC();

    System.out.println("Is prime 7: " + poc.isPrime(7));
    System.out.println("Fibonacci 5: " + poc.fibonacci(5));
    System.out.println("Factorial 5: " + poc.factorial(5));
  }

  public boolean isPrime(int number) {
    if (number <= 1) {
      return false;
    }
    for (int i = 2; i <= Math.sqrt(number); i++) {
      if (number % i == 0) {
        return false;
      }
    }
    return true;
  }

  public int fibonacci(int n) {
    if (n <= 1) {
      return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
  }

  public int factorial(int n) {
    if (n <= 1) {
      return 1;
    }
    return n * factorial(n - 1);
  }

  public String reverseString(String input) {
    return new StringBuilder(input).reverse().toString();
  }

  public List<Integer> getRange(int start, int end) {
    List<Integer> range = new ArrayList<>();
    for (int i = start; i <= end; i++) {
      range.add(i);
    }
    return range;
  }

  public int sumDigits(int number) {
    int sum = 0;
    number = Math.abs(number);
    while (number > 0) {
      sum += number % 10;
      number /= 10;
    }
    return sum;
  }
}
