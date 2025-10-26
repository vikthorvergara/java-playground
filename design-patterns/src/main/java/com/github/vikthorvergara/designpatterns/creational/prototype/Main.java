package com.github.vikthorvergara.designpatterns.creational.prototype;

public class Main {
  public static void main(String[] args) {
    ShapeRegistry registry = new ShapeRegistry();
    registry.loadCache();

    System.out.println("=== Prototype Design Pattern Demo ===\n");

    // Clone a circle from the registry
    Shape clonedCircle1 = registry.getShape("1");
    System.out.println("Cloned Circle 1 (Type: " + clonedCircle1.getType() + ")");
    clonedCircle1.draw();
    System.out.println();

    // Clone another circle and modify it
    Shape clonedCircle2 = registry.getShape("1");
    clonedCircle2.setColor("Yellow");
    if (clonedCircle2 instanceof Circle) {
      ((Circle) clonedCircle2).setRadius(25);
    }
    System.out.println("Cloned Circle 2 (Modified - Type: " + clonedCircle2.getType() + ")");
    clonedCircle2.draw();
    System.out.println();

    // Clone a rectangle
    Shape clonedRectangle = registry.getShape("2");
    System.out.println("Cloned Rectangle (Type: " + clonedRectangle.getType() + ")");
    clonedRectangle.draw();
    System.out.println();

    // Clone a square
    Shape clonedSquare = registry.getShape("3");
    System.out.println("Cloned Square (Type: " + clonedSquare.getType() + ")");
    clonedSquare.draw();
    System.out.println();

    // Create a custom shape and add it to registry
    Circle customCircle = new Circle();
    customCircle.setId("4");
    customCircle.setColor("Purple");
    customCircle.setRadius(30);
    registry.addShape(customCircle.getId(), customCircle);

    // Clone the custom shape
    Shape clonedCustomCircle = registry.getShape("4");
    System.out.println("Cloned Custom Circle (Type: " + clonedCustomCircle.getType() + ")");
    clonedCustomCircle.draw();
  }
}
