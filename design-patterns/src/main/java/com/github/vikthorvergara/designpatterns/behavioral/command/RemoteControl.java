package com.github.vikthorvergara.designpatterns.behavioral.command;

public class RemoteControl {
  private Command command;

  public void setCommand(Command command) {
    this.command = command;
  }

  public void pressButton() {
    if (command != null) {
      command.execute();
    }
  }

  public void pressUndo() {
    if (command != null) {
      command.undo();
    }
  }
}
