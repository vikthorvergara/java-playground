package com.github.vikthorvergara.designpatterns.behavioral.visitor;

public interface ShapeVisitor {
  void visit(Circle circle);

  void visit(Rectangle rectangle);

  void visit(Triangle triangle);
}
