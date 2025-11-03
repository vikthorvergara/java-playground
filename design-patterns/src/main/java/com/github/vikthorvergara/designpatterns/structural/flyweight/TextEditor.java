package com.github.vikthorvergara.designpatterns.structural.flyweight;

import java.util.ArrayList;
import java.util.List;

public class TextEditor {
  private List<CharacterPosition> characters = new ArrayList<>();

  private static class CharacterPosition {
    CharacterFlyweight flyweight;
    int row;
    int column;

    CharacterPosition(CharacterFlyweight flyweight, int row, int column) {
      this.flyweight = flyweight;
      this.row = row;
      this.column = column;
    }
  }

  public void addCharacter(char symbol, String font, int size, int row, int column) {
    CharacterFlyweight flyweight = CharacterFactory.getCharacter(symbol, font, size);
    characters.add(new CharacterPosition(flyweight, row, column));
  }

  public void display() {
    System.out.println("\n=== Document Content ===");
    for (CharacterPosition cp : characters) {
      cp.flyweight.display(cp.row, cp.column);
    }
  }

  public int getTotalCharacters() {
    return characters.size();
  }
}
