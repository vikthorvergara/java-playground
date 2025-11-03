package com.github.vikthorvergara.designpatterns.structural.flyweight;

public class CharacterFlyweight {
  private final char symbol;
  private final String font;
  private final int size;

  public CharacterFlyweight(char symbol, String font, int size) {
    this.symbol = symbol;
    this.font = font;
    this.size = size;
  }

  public void display(int row, int column) {
    System.out.println("Character '" + symbol + "' [Font: " + font + ", Size: " + size
        + "] at position (" + row + ", " + column + ")");
  }

  public char getSymbol() {
    return symbol;
  }
}
