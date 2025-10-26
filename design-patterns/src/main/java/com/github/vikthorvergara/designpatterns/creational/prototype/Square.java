package com.github.vikthorvergara.designpatterns.creational.prototype;

public class Square extends Shape {
  private int side;

  public Square() {
    type = "Square";
  }

  public int getSide() {
    return side;
  }

  public void setSide(int side) {
    this.side = side;
  }

  @Override
  public void draw() {
    System.out.println("Drawing a " + getColor() + " square with side: " + side);
  }
}
