package com.github.vikthorvergara.designpatterns.behavioral.chainofresponsibility;

public class Level3Support extends SupportHandler {
  @Override
  public void handleRequest(SupportRequest request) {
    if (request.getPriority() == SupportRequest.Priority.HIGH
        || request.getPriority() == SupportRequest.Priority.CRITICAL) {
      System.out.println("Level 3 Support: Handling " + request.getPriority()
          + " priority issue - " + request.getIssue());
    } else {
      System.out.println("Level 3 Support: Issue not handled");
    }
  }
}
