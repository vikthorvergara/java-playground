package com.github.vikthorvergara.designpatterns.creational.builder;

public class OfficeComputerBuilder implements ComputerBuilder {
  private Computer computer;

  public OfficeComputerBuilder() {
    this.computer = new Computer();
  }

  @Override
  public void buildCpu() {
    computer.setCpu("Intel Core i5-13400");
  }

  @Override
  public void buildRam() {
    computer.setRam("16GB DDR4 3200MHz");
  }

  @Override
  public void buildStorage() {
    computer.setStorage("512GB SATA SSD");
  }

  @Override
  public void buildGpu() {
    computer.setGpu("Integrated Graphics");
  }

  @Override
  public void buildMotherboard() {
    computer.setMotherboard("MSI B660M Pro");
  }

  @Override
  public void buildPowerSupply() {
    computer.setPowerSupply("500W 80+ Bronze");
  }

  @Override
  public Computer getComputer() {
    return this.computer;
  }
}
