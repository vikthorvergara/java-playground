package com.github.vikthorvergara.junit5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TempDirectoryPOC {

  public static void main(String[] args) throws IOException {
    TempDirectoryPOC poc = new TempDirectoryPOC();

    Path tempDir = Files.createTempDirectory("test");
    Path file = poc.createFile(tempDir, "test.txt", "Hello World");

    System.out.println("File created: " + file);
    System.out.println("Content: " + poc.readFile(file));
    System.out.println("File exists: " + Files.exists(file));

    Files.deleteIfExists(file);
    Files.deleteIfExists(tempDir);
  }

  public Path createFile(Path directory, String filename, String content) throws IOException {
    Path file = directory.resolve(filename);
    Files.writeString(file, content);
    return file;
  }

  public String readFile(Path file) throws IOException {
    return Files.readString(file);
  }

  public void writeLines(Path file, List<String> lines) throws IOException {
    Files.write(file, lines);
  }

  public List<String> readLines(Path file) throws IOException {
    return Files.readAllLines(file);
  }

  public Path createDirectory(Path parent, String dirName) throws IOException {
    return Files.createDirectory(parent.resolve(dirName));
  }

  public boolean fileExists(Path file) {
    return Files.exists(file);
  }

  public long getFileSize(Path file) throws IOException {
    return Files.size(file);
  }
}
