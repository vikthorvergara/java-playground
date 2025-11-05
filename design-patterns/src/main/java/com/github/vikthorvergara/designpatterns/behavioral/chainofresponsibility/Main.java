package com.github.vikthorvergara.designpatterns.behavioral.chainofresponsibility;

public class Main {
  public static void main(String[] args) {
    SupportHandler level1 = new Level1Support();
    SupportHandler level2 = new Level2Support();
    SupportHandler level3 = new Level3Support();

    level1.setNextHandler(level2);
    level2.setNextHandler(level3);

    System.out.println("=== Support Ticket System ===\n");

    SupportRequest request1 = new SupportRequest(
        "Password reset",
        SupportRequest.Priority.LOW
    );

    SupportRequest request2 = new SupportRequest(
        "Software installation issue",
        SupportRequest.Priority.MEDIUM
    );

    SupportRequest request3 = new SupportRequest(
        "Server down",
        SupportRequest.Priority.HIGH
    );

    SupportRequest request4 = new SupportRequest(
        "Data breach security incident",
        SupportRequest.Priority.CRITICAL
    );

    System.out.println("Request 1 (LOW):");
    level1.handleRequest(request1);

    System.out.println("\nRequest 2 (MEDIUM):");
    level1.handleRequest(request2);

    System.out.println("\nRequest 3 (HIGH):");
    level1.handleRequest(request3);

    System.out.println("\nRequest 4 (CRITICAL):");
    level1.handleRequest(request4);
  }
}
