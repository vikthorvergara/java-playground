package com.github.vikthorvergara.designpatterns.structural.facade;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Without Facade (Complex) ===");
    CPU cpu = new CPU();
    Memory memory = new Memory();
    HardDrive hardDrive = new HardDrive();

    cpu.freeze();
    memory.load(0, hardDrive.read(0, 1024));
    cpu.jump(0);
    cpu.execute();

    System.out.println("\n=== With Facade (Simple) ===");
    ComputerFacade computer = new ComputerFacade();
    computer.start();
  }
}
