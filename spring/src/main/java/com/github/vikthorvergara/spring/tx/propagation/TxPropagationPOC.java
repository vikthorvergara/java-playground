package com.github.vikthorvergara.spring.tx.propagation;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

@SpringBootApplication
public class TxPropagationPOC {

    @Bean
    @Primary
    public DataSourceTransactionManager transactionManager(DataSource ds) {
        DataSourceTransactionManager tm = new DataSourceTransactionManager(ds);
        tm.setNestedTransactionAllowed(true);
        return tm;
    }

    @Bean
    public ApplicationRunner schemaInit(JdbcTemplate jdbc) {
        return args -> {
            jdbc.execute("CREATE TABLE accounts (id BIGINT PRIMARY KEY, owner VARCHAR(50), balance BIGINT)");
            jdbc.update("INSERT INTO accounts (id, owner, balance) VALUES (1, 'alice', 1000)");
        };
    }

    @Service
    public static class LedgerService {
        private final JdbcTemplate jdbc;

        public LedgerService(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        long balance(long id) {
            return jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", Long.class, id);
        }

        @Transactional
        public void outerKeepsChangeWhenNestedRollsBack(LedgerService self, long id) {
            jdbc.update("UPDATE accounts SET balance = balance + 1 WHERE id = ?", id);
            try {
                self.nestedBumpThenFail(id);
            } catch (RuntimeException e) {
                System.out.println("  caught nested failure: " + e.getMessage());
            }
        }

        @Transactional(propagation = Propagation.NESTED)
        public void nestedBumpThenFail(long id) {
            jdbc.update("UPDATE accounts SET balance = balance + 100 WHERE id = ?", id);
            throw new RuntimeException("nested boom");
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public boolean mandatory() {
            return TransactionSynchronizationManager.isActualTransactionActive();
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public boolean supports() {
            return TransactionSynchronizationManager.isActualTransactionActive();
        }

        @Transactional
        public boolean callMandatoryWithinTransaction(LedgerService self) {
            return self.mandatory();
        }

        @Transactional
        public boolean callSupportsWithinTransaction(LedgerService self) {
            return self.supports();
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TxPropagationPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            LedgerService ledger = ctx.getBean(LedgerService.class);

            nestedRollsBackToSavepointWhileOuterSurvives(ledger);
            mandatoryRequiresAnExistingTransaction(ledger);
            supportsRunsWithOrWithoutTransaction(ledger);
        }
    }

    static void nestedRollsBackToSavepointWhileOuterSurvives(LedgerService ledger) {
        System.out.println("--- NESTED: inner rolls back to savepoint, outer change survives ---");
        System.out.println("balance before = " + ledger.balance(1));
        ledger.outerKeepsChangeWhenNestedRollsBack(ledger, 1);
        System.out.println("balance after = " + ledger.balance(1) + " (+1 outer kept, +100 nested rolled back)");
    }

    static void mandatoryRequiresAnExistingTransaction(LedgerService ledger) {
        System.out.println("\n--- MANDATORY: fails with no transaction, succeeds inside one ---");
        try {
            ledger.mandatory();
            System.out.println("UNEXPECTED success");
        } catch (IllegalTransactionStateException e) {
            System.out.println("no tx -> " + e.getClass().getSimpleName());
        }
        System.out.println("inside outer tx -> active=" + ledger.callMandatoryWithinTransaction(ledger));
    }

    static void supportsRunsWithOrWithoutTransaction(LedgerService ledger) {
        System.out.println("\n--- SUPPORTS: runs either way, joins a tx only when present ---");
        System.out.println("no tx -> active=" + ledger.supports());
        System.out.println("inside outer tx -> active=" + ledger.callSupportsWithinTransaction(ledger));
    }
}
