package com.github.vikthorvergara.designpatterns.behavioral.visitor;

public class PerimeterCalculator implements ShapeVisitor {
  @Override
  public void visit(Circle circle) {
    double perimeter = 2 * Math.PI * circle.getRadius();
    System.out.printf("Circle perimeter: %.2f%n", perimeter);
  }

  @Override
  public void visit(Rectangle rectangle) {
    double perimeter = 2 * (rectangle.getWidth() + rectangle.getHeight());
    System.out.printf("Rectangle perimeter: %.2f%n", perimeter);
  }

  @Override
  public void visit(Triangle triangle) {
    double perimeter = triangle.getBase() + triangle.getSideA() + triangle.getSideB();
    System.out.printf("Triangle perimeter: %.2f%n", perimeter);
  }
}
