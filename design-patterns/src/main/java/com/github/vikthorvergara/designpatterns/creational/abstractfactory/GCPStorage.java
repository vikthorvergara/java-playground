package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class GCPStorage implements Storage {
  @Override
  public void store(String data) {
    System.out.println("Storing data in GCP Cloud Storage: " + data);
  }
}
