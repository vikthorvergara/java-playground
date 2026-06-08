package com.github.vikthorvergara.spring.data.jpa.locking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class OptimisticLockingPOC {

    @Entity(name = "Stock")
    @Table(name = "stock")
    public static class Stock {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String sku;
        private int quantity;
        @Version
        private long version;

        public Stock() {
        }

        public Stock(String sku, int quantity) {
            this.sku = sku;
            this.quantity = quantity;
        }

        public Long getId() {
            return id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public long getVersion() {
            return version;
        }
    }

    public interface StockRepository extends JpaRepository<Stock, Long> {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OptimisticLockingPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            StockRepository repo = ctx.getBean(StockRepository.class);

            Long id = seed(repo);
            versionStartsAtZeroAndBumpsOnUpdate(repo, id);
            staleUpdateThrowsOptimisticLockingFailure(repo, id);
        }
    }

    static Long seed(StockRepository repo) {
        Stock saved = repo.save(new Stock("WIDGET-1", 100));
        System.out.println("--- seeded Stock id=" + saved.getId() + " quantity=" + saved.getQuantity()
                + " version=" + saved.getVersion() + " ---");
        return saved.getId();
    }

    static void versionStartsAtZeroAndBumpsOnUpdate(StockRepository repo, Long id) {
        System.out.println("\n--- each successful update increments @Version ---");
        Stock s = repo.findById(id).orElseThrow();
        s.setQuantity(90);
        Stock after = repo.saveAndFlush(s);
        System.out.println("quantity=" + after.getQuantity() + " version=" + after.getVersion() + " (0 -> 1)");
    }

    static void staleUpdateThrowsOptimisticLockingFailure(StockRepository repo, Long id) {
        System.out.println("\n--- two readers load same row, second write is stale -> conflict ---");
        Stock first = repo.findById(id).orElseThrow();
        Stock second = repo.findById(id).orElseThrow();
        System.out.println("both loaded at version=" + first.getVersion());

        first.setQuantity(80);
        Stock committed = repo.saveAndFlush(first);
        System.out.println("first writer committed, row now version=" + committed.getVersion() + " (1 -> 2)");

        second.setQuantity(70);
        try {
            repo.saveAndFlush(second);
            System.out.println("UNEXPECTED: stale write succeeded");
        } catch (OptimisticLockingFailureException e) {
            System.out.println("stale write rejected -> " + e.getClass().getSimpleName());
        }
        System.out.println("final quantity = " + repo.findById(id).orElseThrow().getQuantity() + " (80, first writer won)");
    }
}
