package com.github.vikthorvergara.designpatterns.structural.composite;

import java.util.ArrayList;
import java.util.List;

public class Folder implements Component {
  private String name;
  private List<Component> components = new ArrayList<>();

  public Folder(String name) {
    this.name = name;
  }

  public void add(Component component) {
    components.add(component);
  }

  public void remove(Component component) {
    components.remove(component);
  }

  @Override
  public void showDetails() {
    showDetails("");
  }

  @Override
  public void showDetails(String indent) {
    System.out.println(indent + "Folder: " + name);
    for (Component component : components) {
      component.showDetails(indent + "  ");
    }
  }
}
