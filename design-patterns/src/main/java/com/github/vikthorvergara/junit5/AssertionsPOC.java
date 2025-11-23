package com.github.vikthorvergara.junit5;

import java.util.ArrayList;
import java.util.List;

public class AssertionsPOC {

  public static void main(String[] args) {
    AssertionsPOC poc = new AssertionsPOC();

    System.out.println("Square of 5: " + poc.square(5));
    System.out.println("Array sum: " + poc.sumArray(new int[] {1, 2, 3, 4, 5}));
    System.out.println("Is positive 10: " + poc.isPositive(10));
    System.out.println("Is positive -5: " + poc.isPositive(-5));
    System.out.println("Find user by ID: " + poc.findUserById(1));
    System.out.println("Get elements: " + poc.getElements());

    try {
      poc.processValue(-1);
    } catch (IllegalArgumentException e) {
      System.out.println("Caught exception: " + e.getMessage());
    }
  }

  public int square(int n) {
    return n * n;
  }

  public int sumArray(int[] numbers) {
    int sum = 0;
    for (int num : numbers) {
      sum += num;
    }
    return sum;
  }

  public boolean isPositive(int number) {
    return number > 0;
  }

  public User findUserById(int id) {
    if (id == 1) {
      return new User(1, "John");
    }
    return null;
  }

  public List<String> getElements() {
    List<String> elements = new ArrayList<>();
    elements.add("A");
    elements.add("B");
    elements.add("C");
    return elements;
  }

  public void processValue(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Value must be non-negative");
    }
  }

  public String slowOperation() throws InterruptedException {
    Thread.sleep(500);
    return "completed";
  }

  public static class User {
    private final int id;
    private final String name;

    public User(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }
}
