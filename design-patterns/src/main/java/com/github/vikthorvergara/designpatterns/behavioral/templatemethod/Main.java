package com.github.vikthorvergara.designpatterns.behavioral.templatemethod;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Template Method Pattern ===\n");

    System.out.println("Making tea:");
    Beverage tea = new Tea();
    tea.prepareRecipe();

    System.out.println("Making coffee:");
    Beverage coffee = new Coffee();
    coffee.prepareRecipe();

    System.out.println("Making hot chocolate:");
    Beverage hotChocolate = new HotChocolate();
    hotChocolate.prepareRecipe();
  }
}
