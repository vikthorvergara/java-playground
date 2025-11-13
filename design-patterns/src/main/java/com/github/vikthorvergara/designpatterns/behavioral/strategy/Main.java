package com.github.vikthorvergara.designpatterns.behavioral.strategy;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Payment Strategy Pattern ===\n");

    ShoppingCart cart = new ShoppingCart();

    System.out.println("RTX 5090: $2000.00");
    cart.setPaymentStrategy(new CreditCardStrategy("1234567890123456", "Vikthor Vergara"));
    cart.checkout(2000.00);

    System.out.println("\nOLED Monitor: $899.99");
    cart.setPaymentStrategy(new PayPalStrategy("vikthor@vergara.com"));
    cart.checkout(899.99);

    System.out.println("\nMouse: $39.50");
    cart.setPaymentStrategy(new CryptoStrategy("0x123a45Bc6789D0987654e3f212Gh3i4567j8kLm"));
    cart.checkout(39.50);
  }
}
