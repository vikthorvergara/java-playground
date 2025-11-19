package com.github.vikthorvergara.designpatterns.behavioral.visitor;

public class Triangle implements Shape {
  private double base;
  private double height;
  private double sideA;
  private double sideB;

  public Triangle(double base, double height, double sideA, double sideB) {
    this.base = base;
    this.height = height;
    this.sideA = sideA;
    this.sideB = sideB;
  }

  public double getBase() {
    return base;
  }

  public double getHeight() {
    return height;
  }

  public double getSideA() {
    return sideA;
  }

  public double getSideB() {
    return sideB;
  }

  @Override
  public void accept(ShapeVisitor visitor) {
    visitor.visit(this);
  }
}
