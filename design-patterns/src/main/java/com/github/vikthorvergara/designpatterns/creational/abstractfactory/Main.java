package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class Main {
  public static void main(String[] args) {
    CloudFactory awsFactory = new AWSFactory();
    Storage awsStorage = awsFactory.createStorage();
    Database awsDatabase = awsFactory.createDatabase();

    awsStorage.store("AWS data");
    awsDatabase.query("SELECT * FROM aws_table");

    CloudFactory gcpFactory = new GCPFactory();
    Storage gcpStorage = gcpFactory.createStorage();
    Database gcpDatabase = gcpFactory.createDatabase();

    gcpStorage.store("GCP data");
    gcpDatabase.query("SELECT * FROM gcp_table");
  }
}
