package com.github.vikthorvergara.junit5;

public class ParameterizedTestsPOC {

  public static void main(String[] args) {
    ParameterizedTestsPOC poc = new ParameterizedTestsPOC();

    System.out.println("Is palindrome 'radar': " + poc.isPalindrome("radar"));
    System.out.println("Is even 4: " + poc.isEven(4));
    System.out.println("Length of 'hello': " + poc.length("hello"));
    System.out.println("Square of 5: " + poc.square(5));
  }

  public boolean isPalindrome(String text) {
    if (text == null) {
      return false;
    }
    String cleaned = text.toLowerCase().replaceAll("[^a-z0-9]", "");
    return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
  }

  public boolean isEven(int number) {
    return number % 2 == 0;
  }

  public int length(String text) {
    return text != null ? text.length() : 0;
  }

  public int square(int number) {
    return number * number;
  }

  public boolean isPositive(int number) {
    return number > 0;
  }

  public String toUpperCase(String text) {
    return text != null ? text.toUpperCase() : null;
  }

  public int add(int a, int b) {
    return a + b;
  }

  public double divide(double a, double b) {
    if (b == 0) {
      throw new ArithmeticException("Division by zero");
    }
    return a / b;
  }

  public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
  }

  public boolean isWeekend(DayOfWeek day) {
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }

  public boolean isWeekday(DayOfWeek day) {
    return !isWeekend(day);
  }

  public static class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    public boolean isAdult() {
      return age >= 18;
    }
  }
}
