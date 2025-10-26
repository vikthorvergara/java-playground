package com.github.vikthorvergara.designpatterns.creational.prototype;

public abstract class Shape implements Cloneable {
  private String id;
  protected String type;
  private String color;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public abstract void draw();

  @Override
  public Shape clone() {
    try {
      return (Shape) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Failed to clone shape", e);
    }
  }
}
