package com.github.vikthorvergara.spring.tx.isolation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class TxIsolationPOC {

    @Entity
    public static class Account {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String owner;
        private long balance;

        public Account() {
        }

        public Account(String owner, long balance) {
            this.owner = owner;
            this.balance = balance;
        }

        public Long getId() {
            return id;
        }

        public long getBalance() {
            return balance;
        }

        public void setBalance(long balance) {
            this.balance = balance;
        }
    }

    public interface AccountRepository extends JpaRepository<Account, Long> {
    }

    @Service
    public static class LedgerService {
        private final AccountRepository repo;

        public LedgerService(AccountRepository repo) {
            this.repo = repo;
        }

        @Transactional
        public Account open(String owner, long initial) {
            return repo.save(new Account(owner, initial));
        }

        @Transactional(isolation = Isolation.READ_COMMITTED)
        public String runUnderReadCommitted() {
            return currentIsolation();
        }

        @Transactional(isolation = Isolation.SERIALIZABLE)
        public String runUnderSerializable() {
            return currentIsolation();
        }

        @Transactional(readOnly = true)
        public void mutateInsideReadOnly(Long id) {
            System.out.println("  isCurrentTransactionReadOnly=" + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            Account a = repo.findById(id).orElseThrow();
            a.setBalance(a.getBalance() + 5000);
        }

        @Transactional
        public void mutateInsideReadWrite(Long id) {
            Account a = repo.findById(id).orElseThrow();
            a.setBalance(a.getBalance() + 5000);
        }

        static String currentIsolation() {
            Integer level = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
            return isolationName(level) + " (active=" + TransactionSynchronizationManager.isActualTransactionActive() + ")";
        }
    }

    static String isolationName(Integer level) {
        if (level == null) {
            return "connection-default";
        }
        return switch (level) {
            case 1 -> "READ_UNCOMMITTED";
            case 2 -> "READ_COMMITTED";
            case 4 -> "REPEATABLE_READ";
            case 8 -> "SERIALIZABLE";
            default -> "unknown(" + level + ")";
        };
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TxIsolationPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            LedgerService ledger = ctx.getBean(LedgerService.class);
            AccountRepository repo = ctx.getBean(AccountRepository.class);

            Long id = ledger.open("alice", 1000).getId();

            declaredIsolationPropagatesToTransaction(ledger);
            readOnlyTransactionDoesNotFlushChanges(ledger, repo, id);
            readWriteTransactionCommitsChanges(ledger, repo, id);
        }
    }

    static void declaredIsolationPropagatesToTransaction(LedgerService ledger) {
        System.out.println("--- declared @Transactional(isolation=...) reaches the running transaction ---");
        System.out.println("READ_COMMITTED method sees: " + ledger.runUnderReadCommitted());
        System.out.println("SERIALIZABLE  method sees: " + ledger.runUnderSerializable());
    }

    static void readOnlyTransactionDoesNotFlushChanges(LedgerService ledger, AccountRepository repo, Long id) {
        System.out.println("\n--- @Transactional(readOnly=true): dirty change is NOT flushed ---");
        System.out.println("balance before = " + repo.findById(id).orElseThrow().getBalance());
        ledger.mutateInsideReadOnly(id);
        System.out.println("balance after readOnly mutate = " + repo.findById(id).orElseThrow().getBalance() + " (unchanged: flush suppressed)");
    }

    static void readWriteTransactionCommitsChanges(LedgerService ledger, AccountRepository repo, Long id) {
        System.out.println("\n--- normal read-write @Transactional: dirty change IS flushed on commit ---");
        ledger.mutateInsideReadWrite(id);
        System.out.println("balance after read-write mutate = " + repo.findById(id).orElseThrow().getBalance() + " (+5000 committed)");
    }
}
