package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class AWSStorage implements Storage {
  @Override
  public void store(String data) {
    System.out.println("Storing data in AWS S3: " + data);
  }
}
