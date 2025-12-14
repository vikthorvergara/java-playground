package com.github.vikthorvergara.junit5;

public class CsvFileSourcePOC {

  public static void main(String[] args) {
    CsvFileSourcePOC poc = new CsvFileSourcePOC();

    System.out.println("Add: " + poc.add(2, 3));
    System.out.println("Multiply: " + poc.multiply(4, 5));
    System.out.println("Concat: " + poc.concatenate("Hello", "World"));
  }

  public int add(int a, int b) {
    return a + b;
  }

  public int multiply(int a, int b) {
    return a * b;
  }

  public String concatenate(String a, String b) {
    return a + " " + b;
  }

  public boolean isPalindrome(String text) {
    if (text == null) {
      return false;
    }
    String cleaned = text.toLowerCase().replaceAll("[^a-z0-9]", "");
    return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
  }

  public int getStringLength(String text) {
    return text != null ? text.length() : 0;
  }

  public double divide(double a, double b) {
    if (b == 0) {
      throw new ArithmeticException("Division by zero");
    }
    return a / b;
  }

  public String toUpperCase(String text) {
    return text != null ? text.toUpperCase() : null;
  }

  public int calculateDiscount(int price, int discountPercent) {
    return price - (price * discountPercent / 100);
  }
}
