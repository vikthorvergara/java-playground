package com.github.vikthorvergara.junit5;

import java.util.ArrayList;
import java.util.List;

public class LifecyclePOC {

  private List<String> items;

  public static void main(String[] args) {
    LifecyclePOC poc = new LifecyclePOC();
    poc.items = new ArrayList<>();

    System.out.println("Adding items...");
    poc.addItem("Item1");
    poc.addItem("Item2");
    poc.addItem("Item3");

    System.out.println("Items: " + poc.getItems());
    System.out.println("Count: " + poc.getItemCount());

    poc.removeItem("Item2");
    System.out.println("After removal: " + poc.getItems());

    poc.clear();
    System.out.println("After clear: " + poc.getItems());
  }

  public void addItem(String item) {
    if (items == null) {
      items = new ArrayList<>();
    }
    items.add(item);
  }

  public void removeItem(String item) {
    if (items != null) {
      items.remove(item);
    }
  }

  public List<String> getItems() {
    return items != null ? new ArrayList<>(items) : new ArrayList<>();
  }

  public int getItemCount() {
    return items != null ? items.size() : 0;
  }

  public void clear() {
    if (items != null) {
      items.clear();
    }
  }

  public void setItems(List<String> items) {
    this.items = items;
  }
}
