package com.github.vikthorvergara.designpatterns.behavioral.mediator;

public class User {
  private String name;
  private ChatMediator mediator;

  public User(String name, ChatMediator mediator) {
    this.name = name;
    this.mediator = mediator;
  }

  public String getName() {
    return name;
  }

  public void send(String message) {
    System.out.println(name + " sends: " + message);
    mediator.sendMessage(message, this);
  }

  public void receive(String message) {
    System.out.println(name + " received: " + message);
  }
}
