package com.github.vikthorvergara.designpatterns.behavioral.mediator;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Chat Room Mediator ===\n");

    ChatMediator chatRoom = new ChatRoom();

    User vikthor = new User("Vikthor", chatRoom);
    User bob = new User("Bob", chatRoom);
    User chester = new User("Chester", chatRoom);
    User dave = new User("Dave", chatRoom);

    chatRoom.addUser(vikthor);
    chatRoom.addUser(bob);
    chatRoom.addUser(chester);
    chatRoom.addUser(dave);

    System.out.println("\n--- Chat Messages ---\n");

    vikthor.send("Hello everyone!");
  }
}
