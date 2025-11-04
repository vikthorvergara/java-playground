package com.github.vikthorvergara.designpatterns.structural.proxy;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Internet Access with Proxy ===\n");

    Internet internet = new ProxyInternet();

    System.out.println("Attempting to access allowed sites:");
    internet.connectTo("google.com");
    internet.connectTo("github.com");
    internet.connectTo("stackoverflow.com");

    System.out.println("\nAttempting to access blocked sites:");
    internet.connectTo("blocked.com");
    internet.connectTo("banned.com");

    System.out.println("\nAttempting mixed access:");
    internet.connectTo("youtube.com");
    internet.connectTo("restricted.com");
  }
}
