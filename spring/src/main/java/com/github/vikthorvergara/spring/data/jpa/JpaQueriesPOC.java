package com.github.vikthorvergara.spring.data.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class JpaQueriesPOC {

    @Entity(name = "Book")
    @Table(name = "books")
    public static class Book {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;
        private String author;
        private int publishedYear;

        public Book() {
        }

        public Book(String title, String author, int publishedYear) {
            this.title = title;
            this.author = author;
            this.publishedYear = publishedYear;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public int getPublishedYear() {
            return publishedYear;
        }

        @Override
        public String toString() {
            return "Book{" + id + ", " + title + ", " + author + ", " + publishedYear + "}";
        }
    }

    public interface BookRepository extends JpaRepository<Book, Long> {
        List<Book> findByAuthor(String author);

        List<Book> findByPublishedYearGreaterThan(int year);

        long countByAuthor(String author);

        boolean existsByTitle(String title);

        @Query("select b from Book b where lower(b.author) = lower(:author) and b.publishedYear >= :since order by b.publishedYear desc")
        List<Book> searchByAuthorIgnoreCaseSince(@Param("author") String author, @Param("since") int since);

        @Query("select b.author, count(b) from Book b group by b.author")
        List<Object[]> countGroupedByAuthor();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JpaQueriesPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            BookRepository repo = ctx.getBean(BookRepository.class);

            seed(repo);
            derivedFindByAuthor(repo);
            derivedFindByYearGreaterThan(repo);
            derivedCountAndExists(repo);
            jpqlQueryWithNamedParams(repo);
            jpqlAggregateGroupBy(repo);
        }
    }

    static void seed(BookRepository repo) {
        repo.save(new Book("Effective Java", "Bloch", 2018));
        repo.save(new Book("Java Concurrency in Practice", "Goetz", 2006));
        repo.save(new Book("Java Puzzlers", "Bloch", 2005));
        repo.save(new Book("Modern Java in Action", "Urma", 2018));
        repo.save(new Book("Functional Programming in Java", "Saumont", 2017));
    }

    static void derivedFindByAuthor(BookRepository repo) {
        System.out.println("--- derived findByAuthor(\"Bloch\") ---");
        repo.findByAuthor("Bloch").forEach(b -> System.out.println("  " + b));
    }

    static void derivedFindByYearGreaterThan(BookRepository repo) {
        System.out.println("\n--- derived findByPublishedYearGreaterThan(2010) ---");
        repo.findByPublishedYearGreaterThan(2010).forEach(b -> System.out.println("  " + b));
    }

    static void derivedCountAndExists(BookRepository repo) {
        System.out.println("\n--- derived countByAuthor + existsByTitle ---");
        System.out.println("countByAuthor(\"Bloch\") = " + repo.countByAuthor("Bloch"));
        System.out.println("existsByTitle(\"Effective Java\") = " + repo.existsByTitle("Effective Java"));
        System.out.println("existsByTitle(\"Nope\")           = " + repo.existsByTitle("Nope"));
    }

    static void jpqlQueryWithNamedParams(BookRepository repo) {
        System.out.println("\n--- @Query JPQL searchByAuthorIgnoreCaseSince(\"bloch\", 2010) ---");
        repo.searchByAuthorIgnoreCaseSince("bloch", 2010).forEach(b -> System.out.println("  " + b));
    }

    static void jpqlAggregateGroupBy(BookRepository repo) {
        System.out.println("\n--- @Query JPQL aggregate group by author ---");
        repo.countGroupedByAuthor().forEach(row -> System.out.println("  " + row[0] + " = " + row[1]));
    }
}
