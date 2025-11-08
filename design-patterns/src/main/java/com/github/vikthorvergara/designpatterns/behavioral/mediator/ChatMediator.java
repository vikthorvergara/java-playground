package com.github.vikthorvergara.designpatterns.behavioral.mediator;

public interface ChatMediator {
  void sendMessage(String message, User user);
  void addUser(User user);
}
