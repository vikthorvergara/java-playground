package com.github.vikthorvergara.designpatterns.structural.decorator;

public class ExtraSauceDecorator extends BurgerDecorator {
  public ExtraSauceDecorator(Burger burger) {
    super(burger);
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription() + ", Extra Sauce";
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost() + 0.3;
  }
}
