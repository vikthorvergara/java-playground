package com.github.vikthorvergara.designpatterns.behavioral.iterator;

public interface Collection<T> {
  Iterator<T> createIterator();
}
