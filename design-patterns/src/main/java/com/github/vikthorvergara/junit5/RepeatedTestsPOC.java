package com.github.vikthorvergara.junit5;

import java.util.Random;

public class RepeatedTestsPOC {

  private final Random random;

  public static void main(String[] args) {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();

    System.out.println("Random number: " + poc.generateRandomNumber());
    System.out.println("Is stable: " + poc.isStable());

    for (int i = 1; i <= 5; i++) {
      System.out.println("Iteration " + i + ": " + poc.processIteration(i));
    }
  }

  public RepeatedTestsPOC() {
    this.random = new Random();
  }

  public RepeatedTestsPOC(long seed) {
    this.random = new Random(seed);
  }

  public int generateRandomNumber() {
    return random.nextInt(100);
  }

  public boolean isStable() {
    return true;
  }

  public String processIteration(int iteration) {
    return "Processed iteration: " + iteration;
  }

  public int calculateSum(int n) {
    int sum = 0;
    for (int i = 1; i <= n; i++) {
      sum += i;
    }
    return sum;
  }

  public boolean validateConnection() {
    return random.nextInt(10) < 9;
  }
}
