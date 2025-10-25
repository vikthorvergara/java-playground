package com.github.vikthorvergara.designpatterns.creational.builder;

public class Director {
  private ComputerBuilder builder;

  public void setBuilder(ComputerBuilder builder) {
    this.builder = builder;
  }

  public Computer construct() {
    builder.buildCpu();
    builder.buildRam();
    builder.buildStorage();
    builder.buildGpu();
    builder.buildMotherboard();
    builder.buildPowerSupply();
    return builder.getComputer();
  }
}
