package com.github.vikthorvergara.junit5;

public class DependencyInjectionPOC {

  public static void main(String[] args) {
    DependencyInjectionPOC poc = new DependencyInjectionPOC();

    System.out.println("Process data: " + poc.processData("input"));
    System.out.println("Calculate: " + poc.calculate(5, 10));
  }

  public String processData(String input) {
    return "Processed: " + input;
  }

  public int calculate(int a, int b) {
    return a + b;
  }

  public boolean validate(String input) {
    return input != null && input.length() > 0;
  }
}
