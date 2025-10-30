package com.github.vikthorvergara.designpatterns.creational.builder;

public interface ComputerBuilder {
  void buildCpu();

  void buildRam();

  void buildStorage();

  void buildGpu();

  void buildMotherboard();

  void buildPowerSupply();

  Computer getComputer();
}
