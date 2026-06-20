package com.github.vikthorvergara.spring.events.rollback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class AfterRollbackListenerPOC {

    public record WorkDone(String id) {
    }

    @Component
    public static class Listeners {
        final List<String> fired = new ArrayList<>();

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onCommit(WorkDone event) {
            fired.add("commit:" + event.id());
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
        public void onRollback(WorkDone event) {
            fired.add("rollback:" + event.id());
        }
    }

    @Service
    public static class Worker {
        private final ApplicationEventPublisher publisher;

        public Worker(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        @Transactional
        public void commitPath(String id) {
            publisher.publishEvent(new WorkDone(id));
        }

        @Transactional
        public void rollbackPath(String id) {
            publisher.publishEvent(new WorkDone(id));
            throw new RuntimeException("forcing rollback");
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AfterRollbackListenerPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            Worker worker = ctx.getBean(Worker.class);
            Listeners listeners = ctx.getBean(Listeners.class);
            commitPathFiresOnlyAfterCommitListener(worker, listeners);
            rollbackPathFiresOnlyAfterRollbackListener(worker, listeners);
        }
    }

    static void commitPathFiresOnlyAfterCommitListener(Worker worker, Listeners listeners) {
        System.out.println("--- committed tx: only AFTER_COMMIT listener fires ---");
        worker.commitPath("ok");
        System.out.println("fired = " + listeners.fired);
    }

    static void rollbackPathFiresOnlyAfterRollbackListener(Worker worker, Listeners listeners) {
        System.out.println("\n--- rolled-back tx: only AFTER_ROLLBACK listener fires ---");
        try {
            worker.rollbackPath("bad");
        } catch (RuntimeException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        System.out.println("fired = " + listeners.fired);
    }
}
