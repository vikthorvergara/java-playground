package com.github.vikthorvergara.junit5;

public class TestTemplatePOC {

  public static void main(String[] args) {
    TestTemplatePOC poc = new TestTemplatePOC();

    System.out.println("Process with context: " + poc.process("test-context"));
    System.out.println("Calculate: " + poc.calculate(10));
  }

  public String process(String context) {
    return "Processed: " + context;
  }

  public int calculate(int value) {
    return value * 2;
  }

  public boolean validate(String input) {
    return input != null && !input.isEmpty();
  }
}
