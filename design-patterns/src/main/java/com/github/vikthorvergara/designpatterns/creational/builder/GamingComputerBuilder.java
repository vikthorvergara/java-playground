package com.github.vikthorvergara.designpatterns.creational.builder;

public class GamingComputerBuilder implements ComputerBuilder {
  private Computer computer;

  public GamingComputerBuilder() {
    this.computer = new Computer();
  }

  @Override
  public void buildCpu() {
    computer.setCpu("Intel Core i9-13900K");
  }

  @Override
  public void buildRam() {
    computer.setRam("32GB DDR5 6000MHz");
  }

  @Override
  public void buildStorage() {
    computer.setStorage("2TB NVMe SSD");
  }

  @Override
  public void buildGpu() {
    computer.setGpu("NVIDIA RTX 4090");
  }

  @Override
  public void buildMotherboard() {
    computer.setMotherboard("ASUS ROG Maximus Z790");
  }

  @Override
  public void buildPowerSupply() {
    computer.setPowerSupply("1000W 80+ Platinum");
  }

  @Override
  public Computer getComputer() {
    return this.computer;
  }
}
