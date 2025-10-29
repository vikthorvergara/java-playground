package com.github.vikthorvergara.designpatterns.structural.bridge;

public class AdvancedRemote extends BasicRemote {

  public AdvancedRemote(Device device) {
    super(device);
  }

  public void mute() {
    System.out.println("Remote: mute");
    device.setVolume(0);
  }
}
