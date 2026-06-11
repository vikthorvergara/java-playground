package com.github.vikthorvergara.spring.tx.requiresnew;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class RequiresNewPOC {

    @Bean
    public ApplicationRunner schemaInit(JdbcTemplate jdbc) {
        return args -> {
            jdbc.execute("CREATE TABLE orders (id BIGINT PRIMARY KEY, status VARCHAR(20))");
            jdbc.execute("CREATE TABLE audit (id BIGINT AUTO_INCREMENT PRIMARY KEY, message VARCHAR(100))");
        };
    }

    @Service
    public static class OrderService {
        private final JdbcTemplate jdbc;

        public OrderService(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        long orderCount() {
            return jdbc.queryForObject("SELECT count(*) FROM orders", Long.class);
        }

        long auditCount() {
            return jdbc.queryForObject("SELECT count(*) FROM audit", Long.class);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void recordAudit(String message) {
            jdbc.update("INSERT INTO audit (message) VALUES (?)", message);
        }

        @Transactional
        public void placeOrderThenFail(OrderService self, long id) {
            jdbc.update("INSERT INTO orders (id, status) VALUES (?, 'NEW')", id);
            self.recordAudit("attempted order " + id);
            throw new RuntimeException("payment declined");
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RequiresNewPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            OrderService orders = ctx.getBean(OrderService.class);
            requiresNewCommitsIndependentlyOfRolledBackOuter(orders);
        }
    }

    static void requiresNewCommitsIndependentlyOfRolledBackOuter(OrderService orders) {
        System.out.println("--- REQUIRES_NEW: inner audit commits even though outer rolls back ---");
        try {
            orders.placeOrderThenFail(orders, 1);
        } catch (RuntimeException e) {
            System.out.println("  outer failed: " + e.getMessage());
        }
        System.out.println("orders rows = " + orders.orderCount() + " (0, outer rolled back)");
        System.out.println("audit rows  = " + orders.auditCount() + " (1, REQUIRES_NEW committed separately)");
    }
}
