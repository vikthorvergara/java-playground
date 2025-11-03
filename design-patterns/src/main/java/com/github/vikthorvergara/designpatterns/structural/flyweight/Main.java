package com.github.vikthorvergara.designpatterns.structural.flyweight;

public class Main {
  public static void main(String[] args) {
    TextEditor editor = new TextEditor();
    String font = "Arial";
    int size = 12;

    System.out.println("=== Text Editor with Flyweight Pattern ===\n");

    String text = "HELLO WORLD";
    int column = 0;

    for (char c : text.toCharArray()) {
      editor.addCharacter(c, font, size, 0, column++);
    }

    editor.display();

    System.out.println("\n=== Memory Usage Statistics ===");
    System.out.println("Total characters in document: " + editor.getTotalCharacters());
    System.out.println("Unique flyweight objects created: " + CharacterFactory.getFlyweightCount());
    System.out.println("Memory saved: " + (editor.getTotalCharacters() - CharacterFactory.getFlyweightCount())
        + " objects reused!");

    System.out.println("\n=== Adding more text ===");
    String text2 = "HELLO";
    column = 0;
    for (char c : text2.toCharArray()) {
      editor.addCharacter(c, font, size, 1, column++);
    }

    System.out.println("\nTotal characters now: " + editor.getTotalCharacters());
    System.out.println("Unique flyweight objects: " + CharacterFactory.getFlyweightCount()
        + " (notice: same count!)");
  }
}
