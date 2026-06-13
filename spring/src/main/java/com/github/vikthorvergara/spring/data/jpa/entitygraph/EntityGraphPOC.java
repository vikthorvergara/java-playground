package com.github.vikthorvergara.spring.data.jpa.entitygraph;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class EntityGraphPOC {

    @Entity(name = "Writer")
    @Table(name = "writer")
    public static class Writer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Article> articles = new ArrayList<>();

        public Writer() {
        }

        public Writer(String name) {
            this.name = name;
        }

        public void addArticle(String title) {
            Article a = new Article(title, this);
            articles.add(a);
        }

        public List<Article> getArticles() {
            return articles;
        }
    }

    @Entity(name = "Article")
    @Table(name = "article")
    public static class Article {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;
        @ManyToOne(fetch = FetchType.LAZY)
        private Writer writer;

        public Article() {
        }

        public Article(String title, Writer writer) {
            this.title = title;
            this.writer = writer;
        }
    }

    public interface WriterRepository extends JpaRepository<Writer, Long> {
        @EntityGraph(attributePaths = "articles")
        @Query("select distinct w from Writer w")
        List<Writer> findAllWithArticles();
    }

    @Service
    public static class CatalogService {
        private final WriterRepository repo;

        public CatalogService(WriterRepository repo) {
            this.repo = repo;
        }

        @Transactional
        public int countArticlesLazily() {
            int total = 0;
            for (Writer w : repo.findAll()) {
                total += w.getArticles().size();
            }
            return total;
        }

        @Transactional
        public int countArticlesWithGraph() {
            int total = 0;
            for (Writer w : repo.findAllWithArticles()) {
                total += w.getArticles().size();
            }
            return total;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EntityGraphPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setDefaultProperties(java.util.Map.of("spring.jpa.properties.hibernate.generate_statistics", "true"));
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            WriterRepository repo = ctx.getBean(WriterRepository.class);
            CatalogService catalog = ctx.getBean(CatalogService.class);
            Statistics stats = ctx.getBean(EntityManagerFactory.class)
                    .unwrap(SessionFactory.class).getStatistics();

            seed(repo);
            lazyAccessCausesNPlusOneQueries(catalog, stats);
            entityGraphFetchesEverythingInOneQuery(catalog, stats);
        }
    }

    static void seed(WriterRepository repo) {
        for (String name : List.of("Bloch", "Goetz", "Urma")) {
            Writer w = new Writer(name);
            w.addArticle(name + " article 1");
            w.addArticle(name + " article 2");
            repo.save(w);
        }
    }

    static void lazyAccessCausesNPlusOneQueries(CatalogService catalog, Statistics stats) {
        System.out.println("--- lazy findAll then touch each collection: 1 + N queries ---");
        stats.clear();
        int total = catalog.countArticlesLazily();
        System.out.println("articles = " + total + ", statements = " + stats.getPrepareStatementCount()
                + " (1 writers + 3 per-writer article loads)");
    }

    static void entityGraphFetchesEverythingInOneQuery(CatalogService catalog, Statistics stats) {
        System.out.println("\n--- @EntityGraph(attributePaths=articles): single join query ---");
        stats.clear();
        int total = catalog.countArticlesWithGraph();
        System.out.println("articles = " + total + ", statements = " + stats.getPrepareStatementCount()
                + " (1, articles fetched eagerly via the graph)");
    }
}
