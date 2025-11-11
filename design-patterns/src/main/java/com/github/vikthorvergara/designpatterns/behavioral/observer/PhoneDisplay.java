package com.github.vikthorvergara.designpatterns.behavioral.observer;

public class PhoneDisplay implements Observer {
  private String name;

  public PhoneDisplay(String name) {
    this.name = name;
  }

  @Override
  public void update(float temperature) {
    System.out.println(name + " - Phone Display: Temperature is " + temperature + "°C");
  }
}
