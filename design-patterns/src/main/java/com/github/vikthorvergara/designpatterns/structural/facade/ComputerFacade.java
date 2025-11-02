package com.github.vikthorvergara.designpatterns.structural.facade;

public class ComputerFacade {
  private CPU cpu;
  private Memory memory;
  private HardDrive hardDrive;

  public ComputerFacade() {
    this.cpu = new CPU();
    this.memory = new Memory();
    this.hardDrive = new HardDrive();
  }

  public void start() {
    System.out.println("Starting computer...\n");
    cpu.freeze();
    memory.load(0, hardDrive.read(0, 1024));
    cpu.jump(0);
    cpu.execute();
    System.out.println("\nComputer started successfully!");
  }
}
