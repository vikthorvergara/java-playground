package com.github.vikthorvergara.designpatterns.creational.prototype;

import java.util.HashMap;
import java.util.Map;

public class ShapeRegistry {
  private Map<String, Shape> shapeMap = new HashMap<>();

  public void loadCache() {
    // Create and configure prototype shapes
    Circle circle = new Circle();
    circle.setId("1");
    circle.setColor("Red");
    circle.setRadius(10);
    shapeMap.put(circle.getId(), circle);

    Rectangle rectangle = new Rectangle();
    rectangle.setId("2");
    rectangle.setColor("Blue");
    rectangle.setWidth(20);
    rectangle.setHeight(15);
    shapeMap.put(rectangle.getId(), rectangle);

    Square square = new Square();
    square.setId("3");
    square.setColor("Green");
    square.setSide(12);
    shapeMap.put(square.getId(), square);
  }

  public Shape getShape(String shapeId) {
    Shape cachedShape = shapeMap.get(shapeId);
    return cachedShape != null ? cachedShape.clone() : null;
  }

  public void addShape(String id, Shape shape) {
    shapeMap.put(id, shape);
  }
}
