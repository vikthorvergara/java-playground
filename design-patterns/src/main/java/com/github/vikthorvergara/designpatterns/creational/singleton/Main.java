package com.github.vikthorvergara.designpatterns.creational.singleton;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Singleton Design Pattern Demo ===\n");

    // Demonstrate DatabaseConnection Singleton (Eager Initialization)
    System.out.println("--- DatabaseConnection Singleton ---");
    DatabaseConnection db1 = DatabaseConnection.getInstance();
    DatabaseConnection db2 = DatabaseConnection.getInstance();

    // Verify both references point to the same instance
    System.out.println("db1 and db2 are the same instance: " + (db1 == db2));
    System.out.println("db1 hashCode: " + db1.hashCode());
    System.out.println("db2 hashCode: " + db2.hashCode());
    System.out.println();

    // Use the database connection
    db1.connect();
    db1.executeQuery("SELECT * FROM users");
    db2.executeQuery("SELECT * FROM products");
    db1.disconnect();
    System.out.println();

    // Demonstrate ConfigurationManager Singleton (Lazy Initialization)
    System.out.println("--- ConfigurationManager Singleton ---");
    ConfigurationManager config1 = ConfigurationManager.getInstance();
    ConfigurationManager config2 = ConfigurationManager.getInstance();

    // Verify both references point to the same instance
    System.out.println("config1 and config2 are the same instance: " + (config1 == config2));
    System.out.println("config1 hashCode: " + config1.hashCode());
    System.out.println("config2 hashCode: " + config2.hashCode());
    System.out.println();

    // Use the configuration manager
    System.out.println("App Name: " + config1.getConfig("app.name"));
    System.out.println("App Version: " + config1.getConfig("app.version"));
    System.out.println();

    // Modify configuration through one reference
    config1.setConfig("app.environment", "production");
    config2.setConfig("max.threads", "50");

    // Verify changes are reflected in both references
    System.out.println("\nVerifying shared state:");
    System.out.println("config1 environment: " + config1.getConfig("app.environment"));
    System.out.println("config2 environment: " + config2.getConfig("app.environment"));
    System.out.println("config1 max.threads: " + config1.getConfig("max.threads"));
    System.out.println("config2 max.threads: " + config2.getConfig("max.threads"));

    // Display all configurations
    config1.displayAllConfigurations();
  }
}
