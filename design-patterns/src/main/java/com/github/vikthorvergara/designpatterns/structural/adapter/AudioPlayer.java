package com.github.vikthorvergara.designpatterns.structural.adapter;

public class AudioPlayer implements MediaPlayer {
  private MediaAdapter mediaAdapter;

  @Override
  public void play(String audioType, String fileName) {
    // Built-in support for mp3 files
    if (audioType.equalsIgnoreCase("mp3")) {
      System.out.println("Playing mp3 file: " + fileName);
    }
    // Use adapter for other formats
    else if (audioType.equalsIgnoreCase("vlc") || audioType.equalsIgnoreCase("mp4")) {
      mediaAdapter = new MediaAdapter(audioType);
      mediaAdapter.play(audioType, fileName);
    } else {
      System.out.println("Invalid media type: " + audioType + " format not supported");
    }
  }
}
