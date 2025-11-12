package com.github.vikthorvergara.designpatterns.behavioral.state;

public class StoppedState implements State {
  @Override
  public void play(MediaPlayer player) {
    System.out.println("Starting playback");
    player.setState(new PlayingState());
  }

  @Override
  public void pause(MediaPlayer player) {
    System.out.println("Cannot pause - player is stopped");
  }

  @Override
  public void stop(MediaPlayer player) {
    System.out.println("Already stopped");
  }
}
