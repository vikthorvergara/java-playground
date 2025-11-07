package com.github.vikthorvergara.designpatterns.behavioral.iterator;

public class Main {
  public static void main(String[] args) {
    System.out.println("=== Library Book Collection ===\n");

    BookCollection library = new BookCollection();

    library.addBook(new Book("Design Patterns", "Gang of Four"));
    library.addBook(new Book("Clean Code", "Robert C. Martin"));
    library.addBook(new Book("Principles of Software Architecture Modernization", "Diego Pacheco"));
    library.addBook(new Book("The Pragmatic Programmer", "Andrew Hunt"));
    library.addBook(new Book("Code Complete", "Steve McConnell"));

    Iterator<Book> iterator = library.createIterator();

    System.out.println("---------------------");
    System.out.println("Books in the library:");

    while (iterator.hasNext()) {
      Book book = iterator.next();
      System.out.println("- " + book);
    }

    System.out.println("\nTotal: 5 books in collection");
  }
}
