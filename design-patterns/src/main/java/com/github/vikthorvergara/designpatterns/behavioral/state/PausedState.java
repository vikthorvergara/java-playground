package com.github.vikthorvergara.designpatterns.behavioral.state;

public class PausedState implements State {
  @Override
  public void play(MediaPlayer player) {
    System.out.println("Resuming playback");
    player.setState(new PlayingState());
  }

  @Override
  public void pause(MediaPlayer player) {
    System.out.println("Already paused");
  }

  @Override
  public void stop(MediaPlayer player) {
    System.out.println("Stopping from pause");
    player.setState(new StoppedState());
  }
}
