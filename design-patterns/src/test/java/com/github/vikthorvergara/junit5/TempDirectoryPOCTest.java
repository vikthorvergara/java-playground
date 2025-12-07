package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Temporary Directory Tests")
class TempDirectoryPOCTest {

  private final TempDirectoryPOC poc = new TempDirectoryPOC();

  @Test
  @DisplayName("Create and read file in temp directory")
  void testCreateAndReadFile(@TempDir Path tempDir) throws IOException {
    Path file = poc.createFile(tempDir, "test.txt", "Hello World");

    assertTrue(Files.exists(file));
    assertEquals("Hello World", poc.readFile(file));
  }

  @Test
  @DisplayName("Write and read multiple lines")
  void testWriteAndReadLines(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("lines.txt");
    List<String> lines = List.of("Line 1", "Line 2", "Line 3");

    poc.writeLines(file, lines);
    List<String> readLines = poc.readLines(file);

    assertEquals(3, readLines.size());
    assertEquals("Line 1", readLines.get(0));
    assertEquals("Line 3", readLines.get(2));
  }

  @Test
  @DisplayName("Create subdirectory")
  void testCreateSubdirectory(@TempDir Path tempDir) throws IOException {
    Path subDir = poc.createDirectory(tempDir, "subdir");

    assertTrue(Files.exists(subDir));
    assertTrue(Files.isDirectory(subDir));
  }

  @Test
  @DisplayName("Multiple files in temp directory")
  void testMultipleFiles(@TempDir Path tempDir) throws IOException {
    Path file1 = poc.createFile(tempDir, "file1.txt", "Content 1");
    Path file2 = poc.createFile(tempDir, "file2.txt", "Content 2");
    Path file3 = poc.createFile(tempDir, "file3.txt", "Content 3");

    assertTrue(poc.fileExists(file1));
    assertTrue(poc.fileExists(file2));
    assertTrue(poc.fileExists(file3));

    assertEquals("Content 1", poc.readFile(file1));
    assertEquals("Content 2", poc.readFile(file2));
    assertEquals("Content 3", poc.readFile(file3));
  }

  @Test
  @DisplayName("Check file size")
  void testFileSize(@TempDir Path tempDir) throws IOException {
    String content = "This is a test content";
    Path file = poc.createFile(tempDir, "sized.txt", content);

    long size = poc.getFileSize(file);
    assertEquals(content.length(), size);
  }

  @Test
  @DisplayName("Temp directory is empty initially")
  void testTempDirIsEmpty(@TempDir Path tempDir) throws IOException {
    long count = Files.list(tempDir).count();
    assertEquals(0, count);
  }

  @Test
  @DisplayName("Nested directories")
  void testNestedDirectories(@TempDir Path tempDir) throws IOException {
    Path level1 = poc.createDirectory(tempDir, "level1");
    Path level2 = poc.createDirectory(level1, "level2");
    Path file = poc.createFile(level2, "deep.txt", "Deep content");

    assertTrue(Files.exists(file));
    assertEquals("Deep content", poc.readFile(file));
  }

  @Test
  @DisplayName("Temp dir as method parameter and field")
  void testTempDirParameter(@TempDir Path methodTempDir) throws IOException {
    Path file = poc.createFile(methodTempDir, "method.txt", "Method temp dir");
    assertTrue(Files.exists(file));
  }

  @Test
  @DisplayName("Empty file creation")
  void testEmptyFile(@TempDir Path tempDir) throws IOException {
    Path emptyFile = poc.createFile(tempDir, "empty.txt", "");

    assertTrue(Files.exists(emptyFile));
    assertEquals(0, poc.getFileSize(emptyFile));
  }

  @Test
  @DisplayName("Large content file")
  void testLargeContentFile(@TempDir Path tempDir) throws IOException {
    StringBuilder largeContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeContent.append("Line ").append(i).append("\n");
    }

    Path file = poc.createFile(tempDir, "large.txt", largeContent.toString());

    assertTrue(Files.exists(file));
    assertTrue(poc.getFileSize(file) > 1000);
  }
}
