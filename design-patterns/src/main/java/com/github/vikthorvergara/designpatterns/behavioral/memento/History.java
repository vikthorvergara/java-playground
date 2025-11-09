package com.github.vikthorvergara.designpatterns.behavioral.memento;

import java.util.Stack;

public class History {
  private Stack<Memento> mementos = new Stack<>();

  public void push(Memento memento) {
    mementos.push(memento);
  }

  public Memento pop() {
    if (!mementos.isEmpty()) {
      return mementos.pop();
    }
    return null;
  }
}
