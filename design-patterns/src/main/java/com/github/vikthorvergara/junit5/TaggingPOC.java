package com.github.vikthorvergara.junit5;

public class TaggingPOC {

  public static void main(String[] args) {
    TaggingPOC api = new TaggingPOC();

    System.out.println("Fast operation: " + api.fastOperation());
    System.out.println("User creation: " + api.createUser("John"));
    System.out.println("Database query: " + api.queryDatabase("SELECT * FROM users"));
    System.out.println("API call: " + api.callExternalAPI());
  }

  public String fastOperation() {
    return "fast-result";
  }

  public String slowOperation() throws InterruptedException {
    Thread.sleep(500);
    return "slow-result";
  }

  public String createUser(String name) {
    return "User " + name + " created";
  }

  public String updateUser(int id, String name) {
    return "User " + id + " updated to " + name;
  }

  public String deleteUser(int id) {
    return "User " + id + " deleted";
  }

  public String queryDatabase(String query) {
    return "Query result: " + query;
  }

  public String callExternalAPI() {
    return "API response";
  }

  public boolean isProductionReady() {
    return true;
  }

  public String developmentFeature() {
    return "dev-feature";
  }
}
