package com.github.vikthorvergara.designpatterns.creational.builder;

public class Main {
  public static void main(String[] args) {
    Director director = new Director();

    ComputerBuilder gamingBuilder = new GamingComputerBuilder();
    director.setBuilder(gamingBuilder);
    Computer gamingComputer = director.construct();
    System.out.println("Gaming Computer:");
    gamingComputer.showSpecs();

    ComputerBuilder officeBuilder = new OfficeComputerBuilder();
    director.setBuilder(officeBuilder);
    Computer officeComputer = director.construct();
    System.out.println("Office Computer:");
    officeComputer.showSpecs();
  }
}
