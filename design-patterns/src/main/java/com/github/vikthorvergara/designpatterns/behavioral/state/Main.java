package com.github.vikthorvergara.designpatterns.behavioral.state;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Media Player State ===\n");

    MediaPlayer player = new MediaPlayer();

    System.out.println("Initial state (Stopped):");
    player.stop();
    player.pause();

    System.out.println("\nStarting player:");
    player.play();

    System.out.println("\nTrying to play again:");
    player.play();

    System.out.println("\nPausing:");
    player.pause();

    System.out.println("\nResuming:");
    player.play();

    System.out.println("\nStopping:");
    player.stop();

    System.out.println("\nTrying to stop again:");
    player.stop();
  }
}
