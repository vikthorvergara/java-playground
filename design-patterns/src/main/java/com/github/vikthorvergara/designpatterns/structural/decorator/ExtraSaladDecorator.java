package com.github.vikthorvergara.designpatterns.structural.decorator;

public class ExtraSaladDecorator extends BurgerDecorator {
  public ExtraSaladDecorator(Burger burger) {
    super(burger);
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription() + ", Extra Salad";
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost() + 0.5;
  }
}
