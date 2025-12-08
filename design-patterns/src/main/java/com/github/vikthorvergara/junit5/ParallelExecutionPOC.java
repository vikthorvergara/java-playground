package com.github.vikthorvergara.junit5;

public class ParallelExecutionPOC {

  public static void main(String[] args) {
    ParallelExecutionPOC poc = new ParallelExecutionPOC();

    System.out.println("Task 1: " + poc.executeTask(1));
    System.out.println("Task 2: " + poc.executeTask(2));
    System.out.println("Task 3: " + poc.executeTask(3));
  }

  public String executeTask(int taskId) {
    return "Task " + taskId + " executed on thread " + Thread.currentThread().getName();
  }

  public int compute(int value) {
    return value * value;
  }

  public boolean process(String input) {
    return input != null && !input.isEmpty();
  }
}
