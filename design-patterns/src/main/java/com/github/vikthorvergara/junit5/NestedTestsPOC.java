package com.github.vikthorvergara.junit5;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class NestedTestsPOC<T> {

  private final List<T> elements;
  private final int capacity;

  public static void main(String[] args) {
    NestedTestsPOC<String> stack = new NestedTestsPOC<>(5);

    System.out.println("Is empty: " + stack.isEmpty());

    stack.push("First");
    stack.push("Second");
    stack.push("Third");

    System.out.println("Size: " + stack.size());
    System.out.println("Peek: " + stack.peek());
    System.out.println("Pop: " + stack.pop());
    System.out.println("Size after pop: " + stack.size());
    System.out.println("Is full: " + stack.isFull());
  }

  public NestedTestsPOC() {
    this(10);
  }

  public NestedTestsPOC(int capacity) {
    this.capacity = capacity;
    this.elements = new ArrayList<>(capacity);
  }

  public void push(T element) {
    if (isFull()) {
      throw new IllegalStateException("Stack is full");
    }
    elements.add(element);
  }

  public T pop() {
    if (isEmpty()) {
      throw new EmptyStackException();
    }
    return elements.remove(elements.size() - 1);
  }

  public T peek() {
    if (isEmpty()) {
      throw new EmptyStackException();
    }
    return elements.get(elements.size() - 1);
  }

  public boolean isEmpty() {
    return elements.isEmpty();
  }

  public boolean isFull() {
    return elements.size() >= capacity;
  }

  public int size() {
    return elements.size();
  }

  public void clear() {
    elements.clear();
  }
}
