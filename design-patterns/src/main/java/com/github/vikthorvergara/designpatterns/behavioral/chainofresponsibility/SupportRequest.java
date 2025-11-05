package com.github.vikthorvergara.designpatterns.behavioral.chainofresponsibility;

public class SupportRequest {
  private String issue;
  private Priority priority;

  public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL
  }

  public SupportRequest(String issue, Priority priority) {
    this.issue = issue;
    this.priority = priority;
  }

  public String getIssue() {
    return issue;
  }

  public Priority getPriority() {
    return priority;
  }
}
