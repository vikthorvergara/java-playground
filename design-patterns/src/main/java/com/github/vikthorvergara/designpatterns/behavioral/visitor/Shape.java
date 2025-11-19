package com.github.vikthorvergara.designpatterns.behavioral.visitor;

public interface Shape {
  void accept(ShapeVisitor visitor);
}
