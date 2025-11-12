package com.github.vikthorvergara.designpatterns.behavioral.state;

public interface State {
  void play(MediaPlayer player);

  void pause(MediaPlayer player);

  void stop(MediaPlayer player);
}
