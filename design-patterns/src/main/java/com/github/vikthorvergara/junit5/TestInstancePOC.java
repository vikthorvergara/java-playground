package com.github.vikthorvergara.junit5;

public class TestInstancePOC {

  private int counter = 0;

  public static void main(String[] args) {
    TestInstancePOC poc = new TestInstancePOC();

    poc.increment();
    poc.increment();
    poc.increment();

    System.out.println("Counter: " + poc.getCounter());
  }

  public void increment() {
    counter++;
  }

  public int getCounter() {
    return counter;
  }

  public void reset() {
    counter = 0;
  }

  public void setCounter(int value) {
    counter = value;
  }
}
