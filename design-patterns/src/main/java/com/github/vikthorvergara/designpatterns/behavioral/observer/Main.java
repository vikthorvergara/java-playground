package com.github.vikthorvergara.designpatterns.behavioral.observer;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Weather Station Observer ===\n");

    WeatherStation weatherStation = new WeatherStation();

    PhoneDisplay phone1 = new PhoneDisplay("Vikthor's Phone");
    PhoneDisplay phone2 = new PhoneDisplay("Andressa's Phone");
    WindowDisplay window = new WindowDisplay("Living Room");

    weatherStation.registerObserver(phone1);
    weatherStation.registerObserver(phone2);
    weatherStation.registerObserver(window);

    weatherStation.setTemperature(25.5f);

    weatherStation.setTemperature(28.0f);

    System.out.println("---");
    weatherStation.removeObserver(phone2);

    weatherStation.setTemperature(22.3f);
  }
}
