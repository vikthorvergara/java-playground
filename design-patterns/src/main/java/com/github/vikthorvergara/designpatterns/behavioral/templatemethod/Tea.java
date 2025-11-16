package com.github.vikthorvergara.designpatterns.behavioral.templatemethod;

public class Tea extends Beverage {
  @Override
  protected void brew() {
    System.out.println("Steeping the tea");
  }

  @Override
  protected void addCondiments() {
    System.out.println("Adding mate");
  }

  @Override
  protected String getBeverageName() {
    return "tea";
  }
}
