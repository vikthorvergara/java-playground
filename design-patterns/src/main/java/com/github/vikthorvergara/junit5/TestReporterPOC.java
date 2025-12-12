package com.github.vikthorvergara.junit5;

import java.util.HashMap;
import java.util.Map;

public class TestReporterPOC {

  public static void main(String[] args) {
    TestReporterPOC poc = new TestReporterPOC();

    System.out.println("Process user: " + poc.processUser("John", 25));
    System.out.println("Calculate: " + poc.calculate(10, 20));
  }

  public String processUser(String name, int age) {
    return "User: " + name + ", Age: " + age;
  }

  public int calculate(int a, int b) {
    return a + b;
  }

  public Map<String, Object> getUserData(String username) {
    Map<String, Object> data = new HashMap<>();
    data.put("username", username);
    data.put("active", true);
    data.put("loginCount", 5);
    return data;
  }

  public boolean validateInput(String input) {
    return input != null && input.length() > 3;
  }

  public double performCalculation(double x, double y, String operation) {
    switch (operation) {
      case "add":
        return x + y;
      case "subtract":
        return x - y;
      case "multiply":
        return x * y;
      case "divide":
        if (y == 0) {
          throw new ArithmeticException("Division by zero");
        }
        return x / y;
      default:
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }
  }
}
