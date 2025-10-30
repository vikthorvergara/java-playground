package com.github.vikthorvergara.designpatterns.creational.factorymethod;

public class S3 implements BlobStore {
  @Override
  public void save(String data) {
    System.out.println("Saving data to S3: " + data);
  }

  @Override
  public String get(String id) {
    System.out.println("Getting data from S3 with id: " + id);
    return "Data from S3 with id: " + id;
  }
}
