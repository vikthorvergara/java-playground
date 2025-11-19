package com.github.vikthorvergara.designpatterns.behavioral.visitor;

public class AreaCalculator implements ShapeVisitor {
  @Override
  public void visit(Circle circle) {
    double area = Math.PI * circle.getRadius() * circle.getRadius();
    System.out.printf("Circle area: %.2f%n", area);
  }

  @Override
  public void visit(Rectangle rectangle) {
    double area = rectangle.getWidth() * rectangle.getHeight();
    System.out.printf("Rectangle area: %.2f%n", area);
  }

  @Override
  public void visit(Triangle triangle) {
    double area = 0.5 * triangle.getBase() * triangle.getHeight();
    System.out.printf("Triangle area: %.2f%n", area);
  }
}
