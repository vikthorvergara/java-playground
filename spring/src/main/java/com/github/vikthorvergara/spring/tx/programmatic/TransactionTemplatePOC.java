package com.github.vikthorvergara.spring.tx.programmatic;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootApplication
public class TransactionTemplatePOC {

    @Bean
    public ApplicationRunner schemaInit(JdbcTemplate jdbc) {
        return args -> jdbc.execute("CREATE TABLE entries (id BIGINT AUTO_INCREMENT PRIMARY KEY, label VARCHAR(50))");
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager tm) {
        return new TransactionTemplate(tm);
    }

    @Service
    public static class EntryService {
        private final JdbcTemplate jdbc;
        private final TransactionTemplate tx;

        public EntryService(JdbcTemplate jdbc, TransactionTemplate tx) {
            this.jdbc = jdbc;
            this.tx = tx;
        }

        long count() {
            return jdbc.queryForObject("SELECT count(*) FROM entries", Long.class);
        }

        long insertReturningId(String label) {
            return tx.execute(status -> {
                jdbc.update("INSERT INTO entries (label) VALUES (?)", label);
                return jdbc.queryForObject("SELECT max(id) FROM entries", Long.class);
            });
        }

        void insertThenMarkRollbackOnly(String label) {
            tx.executeWithoutResult(status -> {
                jdbc.update("INSERT INTO entries (label) VALUES (?)", label);
                status.setRollbackOnly();
            });
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TransactionTemplatePOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            EntryService entries = ctx.getBean(EntryService.class);
            executeCommitsAndReturnsValue(entries);
            setRollbackOnlyDiscardsTheInsert(entries);
        }
    }

    static void executeCommitsAndReturnsValue(EntryService entries) {
        System.out.println("--- TransactionTemplate.execute commits and returns a value ---");
        long id = entries.insertReturningId("committed");
        System.out.println("inserted id=" + id + ", rows=" + entries.count() + " (1, committed)");
    }

    static void setRollbackOnlyDiscardsTheInsert(EntryService entries) {
        System.out.println("\n--- status.setRollbackOnly() rolls the programmatic tx back ---");
        entries.insertThenMarkRollbackOnly("doomed");
        System.out.println("rows=" + entries.count() + " (still 1, doomed insert rolled back)");
    }
}
