package com.github.vikthorvergara.junit5;

public class TestExecutionListenerPOC {

  private int counter = 0;

  public static void main(String[] args) {
    TestExecutionListenerPOC poc = new TestExecutionListenerPOC();

    System.out.println("Increment: " + poc.increment());
    System.out.println("Double value: " + poc.doubleValue(5));
    System.out.println("Get counter: " + poc.getCounter());
  }

  public int increment() {
    return ++counter;
  }

  public int getCounter() {
    return counter;
  }

  public void reset() {
    counter = 0;
  }

  public int doubleValue(int value) {
    return value * 2;
  }

  public boolean isPositive(int value) {
    return value > 0;
  }

  public String processMessage(String message) {
    return "Processed: " + message;
  }

  public int add(int a, int b) {
    return a + b;
  }
}
