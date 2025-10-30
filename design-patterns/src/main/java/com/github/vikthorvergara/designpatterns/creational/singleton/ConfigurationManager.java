package com.github.vikthorvergara.designpatterns.creational.singleton;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {
  private static volatile ConfigurationManager instance;

  private Map<String, String> configurations;

  private ConfigurationManager() {
    configurations = new HashMap<>();
    loadDefaultConfigurations();
    System.out.println("ConfigurationManager instance created");
  }

  public static ConfigurationManager getInstance() {
    if (instance == null) {
      synchronized (ConfigurationManager.class) {
        if (instance == null) {
          instance = new ConfigurationManager();
        }
      }
    }
    return instance;
  }

  private void loadDefaultConfigurations() {
    configurations.put("app.name", "Design Patterns Demo");
    configurations.put("app.version", "1.0.0");
    configurations.put("app.environment", "develop");
    configurations.put("max.connections", "100");
  }

  public String getConfig(String key) {
    return configurations.getOrDefault(key, "Configuration not found");
  }

  public void setConfig(String key, String value) {
    configurations.put(key, value);
    System.out.println("Configuration updated: " + key + " = " + value);
  }

  public void displayAllConfigurations() {
    System.out.println("\n=== All Configurations ===");
    configurations.forEach((key, value) -> System.out.println(key + " = " + value));
  }
}
