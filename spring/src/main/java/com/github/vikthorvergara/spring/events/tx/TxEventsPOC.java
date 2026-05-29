package com.github.vikthorvergara.spring.events.tx;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class TxEventsPOC {

    @Entity
    public static class Note {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String text;

        public Note() {
        }

        public Note(String text) {
            this.text = text;
        }

        public Long getId() {
            return id;
        }

        public String getText() {
            return text;
        }
    }

    public interface NoteRepository extends JpaRepository<Note, Long> {
    }

    public record NoteCreated(Long id, String text) {
    }

    @Service
    public static class NoteService {
        private final NoteRepository repo;
        private final ApplicationEventPublisher publisher;

        public NoteService(NoteRepository repo, ApplicationEventPublisher publisher) {
            this.repo = repo;
            this.publisher = publisher;
        }

        @Transactional
        public Note saveAndPublish(String text) {
            Note n = repo.save(new Note(text));
            publisher.publishEvent(new NoteCreated(n.getId(), n.getText()));
            return n;
        }

        @Transactional
        public void saveAndPublishThenFail(String text) {
            Note n = repo.save(new Note(text));
            publisher.publishEvent(new NoteCreated(n.getId(), n.getText()));
            throw new RuntimeException("rollback after publish");
        }
    }

    @Component
    public static class Listeners {
        public static final AtomicInteger EAGER = new AtomicInteger();
        public static final AtomicInteger AFTER_COMMIT = new AtomicInteger();

        @EventListener
        public void onAny(NoteCreated ev) {
            EAGER.incrementAndGet();
            System.out.println("  [@EventListener         ] fired immediately for id=" + ev.id());
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onAfterCommit(NoteCreated ev) {
            AFTER_COMMIT.incrementAndGet();
            System.out.println("  [@TransactionalEventListener AFTER_COMMIT] fired for id=" + ev.id());
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TxEventsPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            NoteService service = ctx.getBean(NoteService.class);
            NoteRepository repo = ctx.getBean(NoteRepository.class);

            commitFiresBothListeners(service);
            rollbackFiresOnlyEagerListener(service, repo);
        }
    }

    static void commitFiresBothListeners(NoteService service) {
        System.out.println("--- commit path: both @EventListener and @TransactionalEventListener fire ---");
        service.saveAndPublish("hello");
        System.out.println("counts: eager=" + Listeners.EAGER.get() + " afterCommit=" + Listeners.AFTER_COMMIT.get());
    }

    static void rollbackFiresOnlyEagerListener(NoteService service, NoteRepository repo) {
        System.out.println("\n--- rollback path: @EventListener still fires, AFTER_COMMIT does NOT ---");
        int eagerBefore = Listeners.EAGER.get();
        int afterBefore = Listeners.AFTER_COMMIT.get();
        try {
            service.saveAndPublishThenFail("doomed");
        } catch (RuntimeException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        System.out.println("delta: eager+" + (Listeners.EAGER.get() - eagerBefore)
                + " afterCommit+" + (Listeners.AFTER_COMMIT.get() - afterBefore));
        System.out.println("rows persisted = " + repo.count() + " (only the committed one)");
    }
}
