package com.github.vikthorvergara.designpatterns.behavioral.chainofresponsibility;

public class Level1Support extends SupportHandler {
  @Override
  public void handleRequest(SupportRequest request) {
    if (request.getPriority() == SupportRequest.Priority.LOW) {
      System.out.println("Level 1 Support: Handling LOW priority issue - " + request.getIssue());
    } else {
      System.out.println("Level 1 Support: Cannot handle, escalating...");
      if (nextHandler != null) {
        nextHandler.handleRequest(request);
      }
    }
  }
}
