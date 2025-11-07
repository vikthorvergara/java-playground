package com.github.vikthorvergara.designpatterns.behavioral.iterator;

public interface Iterator<T> {
  boolean hasNext();
  T next();
}
