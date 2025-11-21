package com.github.vikthorvergara.junit5;

public class BasicAnnotationsPOC {

  public static void main(String[] args) {
    BasicAnnotationsPOC calculator = new BasicAnnotationsPOC();

    System.out.println("Addition: 2 + 3 = " + calculator.add(2, 3));
    System.out.println("Subtraction: 5 - 2 = " + calculator.subtract(5, 2));
    System.out.println("Multiplication: 4 * 3 = " + calculator.multiply(4, 3));
    System.out.println("Division: 10 / 2 = " + calculator.divide(10, 2));
    System.out.println("Is Even: 4 is " + (calculator.isEven(4) ? "even" : "odd"));
    System.out.println("Is Even: 7 is " + (calculator.isEven(7) ? "even" : "odd"));
  }

  public int add(int a, int b) {
    return a + b;
  }

  public int subtract(int a, int b) {
    return a - b;
  }

  public int multiply(int a, int b) {
    return a * b;
  }

  public double divide(int a, int b) {
    if (b == 0) {
      throw new ArithmeticException("Division by zero");
    }
    return (double) a / b;
  }

  public boolean isEven(int number) {
    return number % 2 == 0;
  }

  public String concatenate(String a, String b) {
    if (a == null || b == null) {
      return null;
    }
    return a + b;
  }
}
