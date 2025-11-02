package com.github.vikthorvergara.designpatterns.structural.facade;

public class Memory {
  public void load(long position, byte[] data) {
    System.out.println("Memory: Loading data at position " + position);
  }
}
