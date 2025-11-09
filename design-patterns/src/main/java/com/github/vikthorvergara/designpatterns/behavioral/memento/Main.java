package com.github.vikthorvergara.designpatterns.behavioral.memento;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Text Editor with Undo ===\n");

    TextEditor editor = new TextEditor();
    History history = new History();

    history.push(editor.save());

    editor.write("Hello ");
    System.out.println("Current content: " + editor.getContent());
    history.push(editor.save());

    editor.write("World!");
    System.out.println("Current content: " + editor.getContent());

    System.out.println("\n--- Performing Undo ---\n");

    editor.restore(history.pop());
    System.out.println("After undo: " + editor.getContent());

    editor.restore(history.pop());
    System.out.println("After undo: " + editor.getContent());
  }
}
