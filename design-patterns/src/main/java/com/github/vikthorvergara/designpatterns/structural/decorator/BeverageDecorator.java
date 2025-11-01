package com.github.vikthorvergara.designpatterns.structural.decorator;

public class BeverageDecorator extends BurgerDecorator {
  public BeverageDecorator(Burger burger) {
    super(burger);
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription() + ", Beverage";
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost() + 1.5;
  }
}
