package com.github.vikthorvergara.junit5;

public class ConditionalExecutionPOC {

  public static void main(String[] args) {
    ConditionalExecutionPOC poc = new ConditionalExecutionPOC();

    System.out.println("OS Name: " + poc.getOSName());
    System.out.println("Java Version: " + poc.getJavaVersion());
    System.out.println("Architecture: " + poc.getArchitecture());
  }

  public String getOSName() {
    return System.getProperty("os.name");
  }

  public String getJavaVersion() {
    return System.getProperty("java.version");
  }

  public String getArchitecture() {
    return System.getProperty("os.arch");
  }

  public String executeOSSpecificOperation() {
    String os = getOSName().toLowerCase();
    if (os.contains("windows")) {
      return "Windows operation";
    } else if (os.contains("linux")) {
      return "Linux operation";
    } else if (os.contains("mac")) {
      return "Mac operation";
    }
    return "Unknown OS operation";
  }

  public boolean isJavaVersionSupported() {
    return true;
  }
}
