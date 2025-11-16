package com.github.vikthorvergara.designpatterns.behavioral.templatemethod;

public class Coffee extends Beverage {
  @Override
  protected void brew() {
    System.out.println("Dripping coffee through filter");
  }

  @Override
  protected void addCondiments() {
    System.out.println("Adding cinnamon and milk");
  }

  @Override
  protected String getBeverageName() {
    return "coffee";
  }
}
