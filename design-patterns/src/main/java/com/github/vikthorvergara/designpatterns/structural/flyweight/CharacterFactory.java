package com.github.vikthorvergara.designpatterns.structural.flyweight;

import java.util.HashMap;
import java.util.Map;

public class CharacterFactory {
  private static final Map<String, CharacterFlyweight> flyweights = new HashMap<>();

  public static CharacterFlyweight getCharacter(char symbol, String font, int size) {
    String key = symbol + "-" + font + "-" + size;

    CharacterFlyweight flyweight = flyweights.get(key);

    if (flyweight == null) {
      flyweight = new CharacterFlyweight(symbol, font, size);
      flyweights.put(key, flyweight);
      System.out.println("Creating new flyweight for: '" + symbol + "'");
    }

    return flyweight;
  }

  public static int getFlyweightCount() {
    return flyweights.size();
  }
}
