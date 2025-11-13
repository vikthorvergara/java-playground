package com.github.vikthorvergara.designpatterns.behavioral.strategy;

public class CryptoStrategy implements PaymentStrategy {
  private String walletAddress;

  public CryptoStrategy(String walletAddress) {
    this.walletAddress = walletAddress;
  }

  @Override
  public void pay(double amount) {
    System.out.println("Paid $" + amount + " using Cryptocurrency");
    System.out.println("Wallet: " + walletAddress.substring(0, 10) + "...");
  }
}
