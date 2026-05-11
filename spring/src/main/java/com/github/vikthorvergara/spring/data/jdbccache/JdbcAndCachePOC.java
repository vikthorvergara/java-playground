package com.github.vikthorvergara.spring.data.jdbccache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableCaching
@EnableJdbcRepositories(considerNestedRepositories = true)
public class JdbcAndCachePOC {

    @Table("products")
    public record Product(@Id Long id, String name, BigDecimal price) {
    }

    public interface ProductRepository extends CrudRepository<Product, Long> {
    }

    @Service
    public static class ProductService {
        private static final AtomicInteger REPO_CALLS = new AtomicInteger();
        private final ProductRepository repo;

        public ProductService(ProductRepository repo) {
            this.repo = repo;
        }

        @Cacheable("products")
        public Product findById(Long id) {
            REPO_CALLS.incrementAndGet();
            return repo.findById(id).orElseThrow();
        }

        public int repoCallCount() {
            return REPO_CALLS.get();
        }
    }

    @Bean
    public org.springframework.boot.ApplicationRunner schemaInit(JdbcTemplate jdbc) {
        return args -> jdbc.execute(
                "CREATE TABLE IF NOT EXISTS \"products\" (\"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"name\" VARCHAR(255), \"price\" DECIMAL(10,2))");
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JdbcAndCachePOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            ProductRepository repo = ctx.getBean(ProductRepository.class);
            ProductService service = ctx.getBean(ProductService.class);

            seedRows(repo);
            firstQueryHitsRepo(service);
            secondQuerySameIdHitsCache(service);
            differentIdHitsRepoAgain(service);
        }
    }

    static void seedRows(ProductRepository repo) {
        System.out.println("--- seed rows via Spring Data JDBC CrudRepository ---");
        Product saved1 = repo.save(new Product(null, "keyboard", new BigDecimal("129.90")));
        Product saved2 = repo.save(new Product(null, "monitor", new BigDecimal("899.00")));
        System.out.println("saved: " + saved1);
        System.out.println("saved: " + saved2);
        System.out.println("count = " + repo.count());
    }

    static void firstQueryHitsRepo(ProductService service) {
        System.out.println("\n--- first call (cache miss) ---");
        Product p = service.findById(1L);
        System.out.println("loaded: " + p);
        System.out.println("repoCalls = " + service.repoCallCount());
    }

    static void secondQuerySameIdHitsCache(ProductService service) {
        System.out.println("\n--- second call same id (cache hit) ---");
        Product p = service.findById(1L);
        System.out.println("loaded: " + p);
        System.out.println("repoCalls = " + service.repoCallCount() + " (unchanged means cache served it)");
    }

    static void differentIdHitsRepoAgain(ProductService service) {
        System.out.println("\n--- different id (cache miss again) ---");
        Product p = service.findById(2L);
        System.out.println("loaded: " + p);
        System.out.println("repoCalls = " + service.repoCallCount());
    }
}
