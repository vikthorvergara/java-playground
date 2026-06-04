package com.github.vikthorvergara.spring.tx.rollback;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class RollbackRulesPOC {

    public static class BillingException extends Exception {
        public BillingException(String message) {
            super(message);
        }
    }

    @Bean
    public ApplicationRunner schemaInit(JdbcTemplate jdbc) {
        return args -> {
            jdbc.execute("CREATE TABLE entries (id BIGINT PRIMARY KEY, note VARCHAR(50))");
        };
    }

    @Service
    public static class LedgerService {
        private final JdbcTemplate jdbc;

        public LedgerService(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        long count() {
            return jdbc.queryForObject("SELECT count(*) FROM entries", Long.class);
        }

        @Transactional
        public void checkedExceptionDoesNotRollBackByDefault(long id) throws BillingException {
            jdbc.update("INSERT INTO entries (id, note) VALUES (?, 'default-checked')", id);
            throw new BillingException("checked, no rollBackFor -> commit kept");
        }

        @Transactional(rollbackFor = BillingException.class)
        public void rollbackForRollsBackCheckedException(long id) throws BillingException {
            jdbc.update("INSERT INTO entries (id, note) VALUES (?, 'rollbackFor-checked')", id);
            throw new BillingException("checked with rollbackFor -> rolled back");
        }

        @Transactional(noRollbackFor = IllegalStateException.class)
        public void noRollbackForKeepsCommitOnRuntimeException(long id) {
            jdbc.update("INSERT INTO entries (id, note) VALUES (?, 'noRollbackFor-runtime')", id);
            throw new IllegalStateException("runtime but noRollbackFor -> commit kept");
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RollbackRulesPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            LedgerService ledger = ctx.getBean(LedgerService.class);

            checkedExceptionCommitsByDefault(ledger);
            rollbackForForcesRollbackOnChecked(ledger);
            noRollbackForCommitsOnRuntime(ledger);
        }
    }

    static void checkedExceptionCommitsByDefault(LedgerService ledger) {
        System.out.println("--- checked exception, no rule -> Spring commits (only RuntimeException/Error roll back by default) ---");
        try {
            ledger.checkedExceptionDoesNotRollBackByDefault(1);
        } catch (BillingException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        System.out.println("rows = " + ledger.count() + " (expected 1, row survived)");
    }

    static void rollbackForForcesRollbackOnChecked(LedgerService ledger) {
        System.out.println("\n--- rollbackFor=BillingException -> checked exception now rolls back ---");
        try {
            ledger.rollbackForRollsBackCheckedException(2);
        } catch (BillingException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        System.out.println("rows = " + ledger.count() + " (still 1, insert rolled back)");
    }

    static void noRollbackForCommitsOnRuntime(LedgerService ledger) {
        System.out.println("\n--- noRollbackFor=IllegalStateException -> runtime exception does NOT roll back ---");
        try {
            ledger.noRollbackForKeepsCommitOnRuntimeException(3);
        } catch (IllegalStateException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        System.out.println("rows = " + ledger.count() + " (now 2, row survived despite runtime exception)");
    }
}
