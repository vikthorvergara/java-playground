package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public interface CloudFactory {
  Storage createStorage();

  Database createDatabase();
}
