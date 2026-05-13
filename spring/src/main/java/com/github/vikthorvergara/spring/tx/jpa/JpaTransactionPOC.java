package com.github.vikthorvergara.spring.tx.jpa;

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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class JpaTransactionPOC {

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

        public String getOwner() {
            return owner;
        }

        public long getBalance() {
            return balance;
        }

        public void setBalance(long balance) {
            this.balance = balance;
        }

        @Override
        public String toString() {
            return "Account{id=" + id + ", owner='" + owner + "', balance=" + balance + "}";
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
        public Account openAccount(String owner, long initial) {
            return repo.save(new Account(owner, initial));
        }

        @Transactional
        public void transferOk(Long from, Long to, long amount) {
            Account a = repo.findById(from).orElseThrow();
            Account b = repo.findById(to).orElseThrow();
            a.setBalance(a.getBalance() - amount);
            b.setBalance(b.getBalance() + amount);
            repo.save(a);
            repo.save(b);
        }

        @Transactional
        public void transferAndFail(Long from, Long to, long amount) {
            Account a = repo.findById(from).orElseThrow();
            Account b = repo.findById(to).orElseThrow();
            a.setBalance(a.getBalance() - amount);
            b.setBalance(b.getBalance() + amount);
            repo.save(a);
            repo.save(b);
            throw new RuntimeException("simulated failure after partial work");
        }

        @Transactional
        public void outerWithInnerRequiresNew(Long auditId, Long from, Long to, long amount, LedgerService self) {
            self.appendAudit(auditId);
            transferAndFail(from, to, amount);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void appendAudit(Long auditId) {
            Account audit = repo.findById(auditId).orElseThrow();
            audit.setBalance(audit.getBalance() + 1);
            repo.save(audit);
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JpaTransactionPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            LedgerService ledger = ctx.getBean(LedgerService.class);
            AccountRepository repo = ctx.getBean(AccountRepository.class);

            Long alice = ledger.openAccount("alice", 1000).getId();
            Long bob = ledger.openAccount("bob", 500).getId();
            Long audit = ledger.openAccount("audit", 0).getId();

            commitOnSuccess(ledger, repo, alice, bob);
            rollbackOnException(ledger, repo, alice, bob);
            innerRequiresNewSurvivesOuterRollback(ledger, repo, alice, bob, audit);
        }
    }

    static void commitOnSuccess(LedgerService ledger, AccountRepository repo, Long alice, Long bob) {
        System.out.println("--- commit on success ---");
        printBalances(repo, alice, bob);
        ledger.transferOk(alice, bob, 100);
        System.out.println("after transferOk(100):");
        printBalances(repo, alice, bob);
    }

    static void rollbackOnException(LedgerService ledger, AccountRepository repo, Long alice, Long bob) {
        System.out.println("\n--- rollback on RuntimeException ---");
        printBalances(repo, alice, bob);
        try {
            ledger.transferAndFail(alice, bob, 250);
        } catch (RuntimeException e) {
            System.out.println("caught: " + e.getMessage());
        }
        System.out.println("after rollback (balances unchanged):");
        printBalances(repo, alice, bob);
    }

    static void innerRequiresNewSurvivesOuterRollback(LedgerService ledger, AccountRepository repo,
                                                      Long alice, Long bob, Long audit) {
        System.out.println("\n--- REQUIRES_NEW: inner commits even when outer rolls back ---");
        System.out.println("audit before = " + repo.findById(audit).orElseThrow().getBalance());
        printBalances(repo, alice, bob);
        try {
            ledger.outerWithInnerRequiresNew(audit, alice, bob, 99, ledger);
        } catch (RuntimeException e) {
            System.out.println("caught: " + e.getMessage());
        }
        System.out.println("audit after = " + repo.findById(audit).orElseThrow().getBalance() + " (incremented despite outer rollback)");
        System.out.println("alice/bob unchanged because outer rolled back:");
        printBalances(repo, alice, bob);
    }

    static void printBalances(AccountRepository repo, Long alice, Long bob) {
        System.out.println("  " + repo.findById(alice).orElseThrow());
        System.out.println("  " + repo.findById(bob).orElseThrow());
    }
}
