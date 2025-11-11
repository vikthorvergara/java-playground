package com.github.vikthorvergara.designpatterns.behavioral.observer;

import java.util.ArrayList;
import java.util.List;

public class WeatherStation implements Subject {
  private List<Observer> observers;
  private float temperature;

  public WeatherStation() {
    observers = new ArrayList<>();
  }

  @Override
  public void registerObserver(Observer observer) {
    observers.add(observer);
    System.out.println("Observer registered");
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
    System.out.println("Observer removed");
  }

  @Override
  public void notifyObservers() {
    for (Observer observer : observers) {
      observer.update(temperature);
    }
  }

  public void setTemperature(float temperature) {
    System.out.println("\nWeather Station: Temperature changed to " + temperature + "°C");
    this.temperature = temperature;
    notifyObservers();
  }
}
