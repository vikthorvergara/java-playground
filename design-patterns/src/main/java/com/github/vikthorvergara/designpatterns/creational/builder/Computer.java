package com.github.vikthorvergara.designpatterns.creational.builder;

public class Computer {
  private String cpu;
  private String ram;
  private String storage;
  private String gpu;
  private String motherboard;
  private String powerSupply;

  public void setCpu(String cpu) {
    this.cpu = cpu;
  }

  public void setRam(String ram) {
    this.ram = ram;
  }

  public void setStorage(String storage) {
    this.storage = storage;
  }

  public void setGpu(String gpu) {
    this.gpu = gpu;
  }

  public void setMotherboard(String motherboard) {
    this.motherboard = motherboard;
  }

  public void setPowerSupply(String powerSupply) {
    this.powerSupply = powerSupply;
  }

  public void showSpecs() {
    System.out.println("Computer Specifications:");
    System.out.println("CPU: " + cpu);
    System.out.println("RAM: " + ram);
    System.out.println("Storage: " + storage);
    System.out.println("GPU: " + gpu);
    System.out.println("Motherboard: " + motherboard);
    System.out.println("Power Supply: " + powerSupply);
  }
}
