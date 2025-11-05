package com.github.vikthorvergara.designpatterns.behavioral.chainofresponsibility;

public class Level2Support extends SupportHandler {
  @Override
  public void handleRequest(SupportRequest request) {
    if (request.getPriority() == SupportRequest.Priority.MEDIUM) {
      System.out.println("Level 2 Support: Handling MEDIUM priority issue - " + request.getIssue());
    } else {
      System.out.println("Level 2 Support: Cannot handle, escalating...");
      if (nextHandler != null) {
        nextHandler.handleRequest(request);
      }
    }
  }
}
