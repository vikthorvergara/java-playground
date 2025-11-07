package com.github.vikthorvergara.designpatterns.behavioral.iterator;

import java.util.ArrayList;
import java.util.List;

public class BookCollection implements Collection<Book> {
  private List<Book> books;

  public BookCollection() {
    this.books = new ArrayList<>();
  }

  public void addBook(Book book) {
    books.add(book);
  }

  @Override
  public Iterator<Book> createIterator() {
    return new BookIterator(books);
  }

  private class BookIterator implements Iterator<Book> {
    private List<Book> books;
    private int position = 0;

    public BookIterator(List<Book> books) {
      this.books = books;
    }

    @Override
    public boolean hasNext() {
      return position < books.size();
    }

    @Override
    public Book next() {
      if (hasNext()) {
        return books.get(position++);
      }
      return null;
    }
  }
}
