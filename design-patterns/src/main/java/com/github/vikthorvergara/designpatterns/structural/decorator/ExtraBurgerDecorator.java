package com.github.vikthorvergara.designpatterns.structural.decorator;

public class ExtraBurgerDecorator extends BurgerDecorator {
  public ExtraBurgerDecorator(Burger burger) {
    super(burger);
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription() + ", Extra Burger Patty";
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost() + 3.0;
  }
}
