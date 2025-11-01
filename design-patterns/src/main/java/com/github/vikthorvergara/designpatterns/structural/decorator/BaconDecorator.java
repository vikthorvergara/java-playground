package com.github.vikthorvergara.designpatterns.structural.decorator;

public class BaconDecorator extends BurgerDecorator {
  public BaconDecorator(Burger burger) {
    super(burger);
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription() + ", Bacon";
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost() + 1.5;
  }
}
