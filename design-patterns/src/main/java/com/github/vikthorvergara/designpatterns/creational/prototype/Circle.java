package com.github.vikthorvergara.designpatterns.creational.prototype;

public class Circle extends Shape {
  private int radius;

  public Circle() {
    type = "Circle";
  }

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }

  @Override
  public void draw() {
    System.out.println("Drawing a " + getColor() + " circle with radius: " + radius);
  }
}
