package com.github.vikthorvergara.designpatterns.behavioral.templatemethod;

public abstract class Beverage {
  public final void prepareRecipe() {
    boilWater();
    brew();
    pourInCup();
    addCondiments();
    System.out.println("Your " + getBeverageName() + " is ready!\n");
  }

  private void boilWater() {
    System.out.println("Boiling water");
  }

  private void pourInCup() {
    System.out.println("Pouring into cup");
  }

  protected abstract void brew();

  protected abstract void addCondiments();

  protected abstract String getBeverageName();
}
