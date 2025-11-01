package com.github.vikthorvergara.designpatterns.structural.decorator;

public class FrenchFriesDecorator extends BurgerDecorator {
  public FrenchFriesDecorator(Burger burger) {
    super(burger);
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription() + ", French Fries";
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost() + 2.5;
  }
}
