package com.github.vikthorvergara.designpatterns.creational.singleton;

public class DatabaseConnection {
  private static final DatabaseConnection instance = new DatabaseConnection();

  private String connectionString;
  private boolean isConnected;

  private DatabaseConnection() {
    connectionString = "jdbc:mysql://localhost:3306/mydb";
    isConnected = false;
    System.out.println("DatabaseConnection instance created");
  }

  public static DatabaseConnection getInstance() {
    return instance;
  }

  public void connect() {
    if (!isConnected) {
      System.out.println("Connecting to database: " + connectionString);
      isConnected = true;
    } else {
      System.out.println("Already connected to database");
    }
  }

  public void disconnect() {
    if (isConnected) {
      System.out.println("Disconnecting from database");
      isConnected = false;
    }
  }

  public void executeQuery(String query) {
    if (isConnected) {
      System.out.println("Executing query: " + query);
    } else {
      System.out.println("Error: Not connected to database");
    }
  }

  public String getConnectionString() {
    return connectionString;
  }

  public boolean isConnected() {
    return isConnected;
  }
}
