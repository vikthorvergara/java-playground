package com.github.vikthorvergara.junit5;

public class TimeoutsPOC {

  public static void main(String[] args) throws InterruptedException {
    TimeoutsPOC poc = new TimeoutsPOC();

    System.out.println("Fast operation: " + poc.fastOperation());
    System.out.println("Medium operation: " + poc.mediumOperation());
  }

  public String fastOperation() {
    return "fast";
  }

  public String mediumOperation() throws InterruptedException {
    Thread.sleep(100);
    return "medium";
  }

  public String slowOperation() throws InterruptedException {
    Thread.sleep(2000);
    return "slow";
  }

  public int compute(int n) throws InterruptedException {
    Thread.sleep(50);
    return n * n;
  }

  public void processWithDelay(int milliseconds) throws InterruptedException {
    Thread.sleep(milliseconds);
  }
}
