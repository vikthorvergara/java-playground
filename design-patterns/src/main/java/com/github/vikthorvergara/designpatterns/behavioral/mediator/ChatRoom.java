package com.github.vikthorvergara.designpatterns.behavioral.mediator;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements ChatMediator {
  private List<User> users;

  public ChatRoom() {
    this.users = new ArrayList<>();
  }

  @Override
  public void addUser(User user) {
    users.add(user);
    System.out.println(user.getName() + " joined the chat room");
  }

  @Override
  public void sendMessage(String message, User sender) {
    for (User user : users) {
      // send message to all users but the sender
      if (user != sender) {
        user.receive(message);
      }
    }
  }
}
