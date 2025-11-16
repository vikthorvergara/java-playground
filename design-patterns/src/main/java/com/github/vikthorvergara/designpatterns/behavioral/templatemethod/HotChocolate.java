package com.github.vikthorvergara.designpatterns.behavioral.templatemethod;

public class HotChocolate extends Beverage {
  @Override
  protected void brew() {
    System.out.println("Mixing chocolate powder");
  }

  @Override
  protected void addCondiments() {
    System.out.println("Adding whipped cream and marshmallows");
  }

  @Override
  protected String getBeverageName() {
    return "hot chocolate";
  }
}
