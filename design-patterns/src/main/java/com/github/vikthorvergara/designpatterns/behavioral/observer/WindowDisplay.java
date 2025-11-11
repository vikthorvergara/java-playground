package com.github.vikthorvergara.designpatterns.behavioral.observer;

public class WindowDisplay implements Observer {
  private String location;

  public WindowDisplay(String location) {
    this.location = location;
  }

  @Override
  public void update(float temperature) {
    System.out.println(location + " - Window Display: Current temp is " + temperature + "°C");
  }
}
