package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class AWSDatabase implements Database {
  @Override
  public void query(String sql) {
    System.out.println("Querying AWS RDS: " + sql);
  }
}
