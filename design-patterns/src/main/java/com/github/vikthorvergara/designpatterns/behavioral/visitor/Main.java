package com.github.vikthorvergara.designpatterns.behavioral.visitor;

import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Visitor Pattern ===\n");

    List<Shape> shapes = new ArrayList<>();
    shapes.add(new Circle(5.0));
    shapes.add(new Rectangle(4.0, 6.0));
    shapes.add(new Triangle(3.0, 4.0, 5.0, 5.0));

    System.out.println("Calculating areas:");
    ShapeVisitor areaCalculator = new AreaCalculator();
    for (Shape shape : shapes) {
      shape.accept(areaCalculator);
    }

    System.out.println("\nCalculating perimeters:");
    ShapeVisitor perimeterCalculator = new PerimeterCalculator();
    for (Shape shape : shapes) {
      shape.accept(perimeterCalculator);
    }
  }
}
