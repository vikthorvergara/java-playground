package com.github.vikthorvergara.junit5;

public class CustomExtensionPOC {

  private String data;

  public static void main(String[] args) {
    CustomExtensionPOC poc = new CustomExtensionPOC();
    poc.setData("test");

    System.out.println("Data: " + poc.getData());
    System.out.println("Process: " + poc.process("input"));
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String process(String input) {
    return "Processed: " + input;
  }

  public int calculate(int value) {
    return value * 2;
  }
}
