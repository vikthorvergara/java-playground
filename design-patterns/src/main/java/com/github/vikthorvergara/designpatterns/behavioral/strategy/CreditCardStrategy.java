package com.github.vikthorvergara.designpatterns.behavioral.strategy;

public class CreditCardStrategy implements PaymentStrategy {
  private String cardNumber;
  private String name;

  public CreditCardStrategy(String cardNumber, String name) {
    this.cardNumber = cardNumber;
    this.name = name;
  }

  @Override
  public void pay(double amount) {
    System.out.println("Paid $" + amount + " using Credit Card");
    System.out.println("Card: " + maskCardNumber(cardNumber) + " (" + name + ")");
  }

  private String maskCardNumber(String cardNumber) {
    return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
  }
}
