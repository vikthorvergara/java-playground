package com.github.vikthorvergara.junit5;

public class DisplayNameGeneratorPOC {

  public static void main(String[] args) {
    DisplayNameGeneratorPOC poc = new DisplayNameGeneratorPOC();

    System.out.println("Add: " + poc.add(2, 3));
    System.out.println("Multiply: " + poc.multiply(4, 5));
  }

  public int add(int a, int b) {
    return a + b;
  }

  public int multiply(int a, int b) {
    return a * b;
  }

  public boolean isValid(String input) {
    return input != null && !input.isEmpty();
  }
}
