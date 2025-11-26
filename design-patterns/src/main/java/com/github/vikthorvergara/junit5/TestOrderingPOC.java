package com.github.vikthorvergara.junit5;

public class TestOrderingPOC {

  private int counter = 0;

  public static void main(String[] args) {
    TestOrderingPOC poc = new TestOrderingPOC();

    System.out.println("Increment: " + poc.increment());
    System.out.println("Increment: " + poc.increment());
    System.out.println("Increment: " + poc.increment());
    System.out.println("Current value: " + poc.getValue());
    poc.reset();
    System.out.println("After reset: " + poc.getValue());
  }

  public int increment() {
    return ++counter;
  }

  public int decrement() {
    return --counter;
  }

  public int getValue() {
    return counter;
  }

  public void setValue(int value) {
    counter = value;
  }

  public void reset() {
    counter = 0;
  }

  public boolean isPositive() {
    return counter > 0;
  }

  public boolean isZero() {
    return counter == 0;
  }
}
