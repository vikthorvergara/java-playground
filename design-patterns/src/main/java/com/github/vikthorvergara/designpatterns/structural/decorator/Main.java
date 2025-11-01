package com.github.vikthorvergara.designpatterns.structural.decorator;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Welcome to Burger Shop ===\n");

    Burger burger1 = new BasicBurger();
    System.out.println(burger1.getDescription() + " - $" + burger1.getCost());

    Burger burger2 = new BaconDecorator(new BasicBurger());
    System.out.println(burger2.getDescription() + " - $" + burger2.getCost());

    Burger burger3 = new ExtraSaladDecorator(
        new BaconDecorator(
            new BasicBurger()
        )
    );
    System.out.println(burger3.getDescription() + " - $" + burger3.getCost());

    Burger burger4 = new ExtraSauceDecorator(
        new BaconDecorator(
            new ExtraBurgerDecorator(
                new BasicBurger()
            )
        )
    );
    System.out.println(burger4.getDescription() + " - $" + burger4.getCost());

    Burger deluxeCombo = new BeverageDecorator(
        new FrenchFriesDecorator(
            new ExtraSauceDecorator(
                new ExtraSaladDecorator(
                    new BaconDecorator(
                        new ExtraBurgerDecorator(
                            new BasicBurger()
                        )
                    )
                )
            )
        )
    );
    System.out.println("\n" + deluxeCombo.getDescription() + " - $" + deluxeCombo.getCost());
    System.out.println("\n=== Enjoy your meal! ===");
  }
}
