package com.github.vikthorvergara.junit5;

public class AssumptionsPOC {

  public static void main(String[] args) {
    AssumptionsPOC poc = new AssumptionsPOC();

    System.out.println("OS: " + poc.getOperatingSystem());
    System.out.println("Java Version: " + poc.getJavaVersion());
    System.out.println("Environment: " + poc.getEnvironment());
    System.out.println("Is CI: " + poc.isContinuousIntegration());
    System.out.println("Database connected: " + poc.isDatabaseAvailable());
  }

  public String getOperatingSystem() {
    return System.getProperty("os.name");
  }

  public String getJavaVersion() {
    return System.getProperty("java.version");
  }

  public String getEnvironment() {
    String env = System.getenv("ENV");
    return env != null ? env : "development";
  }

  public boolean isContinuousIntegration() {
    return System.getenv("CI") != null;
  }

  public boolean isDatabaseAvailable() {
    String dbUrl = System.getenv("DATABASE_URL");
    return dbUrl != null && !dbUrl.isEmpty();
  }

  public int performExpensiveCalculation() {
    int result = 0;
    for (int i = 0; i < 1000; i++) {
      result += i;
    }
    return result;
  }

  public String fetchFromExternalService() {
    return "external-data";
  }
}
