package com.github.vikthorvergara.designpatterns.structural.decorator;

public abstract class BurgerDecorator implements Burger {
  protected Burger decoratedBurger;

  public BurgerDecorator(Burger burger) {
    this.decoratedBurger = burger;
  }

  @Override
  public String getDescription() {
    return decoratedBurger.getDescription();
  }

  @Override
  public double getCost() {
    return decoratedBurger.getCost();
  }
}
