package com.github.vikthorvergara.designpatterns.behavioral.command;

public class Main {
  public static void main(String[] args) {
    Light livingRoomLight = new Light("Living Room");
    Light bedroomLight = new Light("Bedroom");

    Command livingRoomOn = new LightOnCommand(livingRoomLight);
    Command livingRoomOff = new LightOffCommand(livingRoomLight);
    Command bedroomOn = new LightOnCommand(bedroomLight);

    RemoteControl remote = new RemoteControl();

    System.out.println("=== Remote Control Demo ===\n");

    System.out.println("Pressing button to turn ON living room light:");
    remote.setCommand(livingRoomOn);
    remote.pressButton();

    System.out.println("\nPressing button to turn OFF living room light:");
    remote.setCommand(livingRoomOff);
    remote.pressButton();

    System.out.println("\nPressing UNDO button:");
    remote.pressUndo();

    System.out.println("\nPressing button to turn ON bedroom light:");
    remote.setCommand(bedroomOn);
    remote.pressButton();

    System.out.println("\nPressing UNDO button:");
    remote.pressUndo();
  }
}
