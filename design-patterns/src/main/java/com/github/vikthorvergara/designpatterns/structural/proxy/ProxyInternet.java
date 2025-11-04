package com.github.vikthorvergara.designpatterns.structural.proxy;

import java.util.ArrayList;
import java.util.List;

public class ProxyInternet implements Internet {
  private RealInternet realInternet = new RealInternet();
  private static List<String> bannedSites;

  static {
    bannedSites = new ArrayList<>();
    bannedSites.add("blocked.com");
    bannedSites.add("banned.com");
    bannedSites.add("restricted.com");
  }

  @Override
  public void connectTo(String serverHost) {
    if (bannedSites.contains(serverHost.toLowerCase())) {
      System.out.println("Access Denied! " + serverHost + " is blocked.");
    } else {
      realInternet.connectTo(serverHost);
    }
  }
}
