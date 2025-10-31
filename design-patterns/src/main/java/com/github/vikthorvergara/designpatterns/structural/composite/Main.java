package com.github.vikthorvergara.designpatterns.structural.composite;

public class Main {
  public static void main(String[] args) {
    File file1 = new File("document.txt", 100);
    File file2 = new File("image.png", 2500);
    File file3 = new File("video.mp4", 50000);

    Folder documentsFolder = new Folder("Documents");
    documentsFolder.add(file1);

    Folder mediaFolder = new Folder("Media");
    mediaFolder.add(file2);
    mediaFolder.add(file3);

    Folder testFolder = new Folder("Test");
    Folder test2Folder = new Folder("Test2");
    Folder test3Folder = new Folder("Test3");
    testFolder.add(test2Folder);
    test2Folder.add(test3Folder);

    Folder rootFolder = new Folder("Root");
    rootFolder.add(testFolder);
    rootFolder.add(documentsFolder);
    rootFolder.add(mediaFolder);
    rootFolder.add(new File("readme.md", 10));

    System.out.println("File System Structure:");
    System.out.println("======================");
    rootFolder.showDetails();
  }
}
