package com.github.vikthorvergara.spring.data.jpa.auditing;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
@EnableJpaAuditing
public class AuditingPOC {

    @Entity(name = "Doc")
    @Table(name = "doc")
    @EntityListeners(AuditingEntityListener.class)
    public static class Doc {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;
        @CreatedDate
        private Instant createdAt;
        @LastModifiedDate
        private Instant updatedAt;

        public Doc() {
        }

        public Doc(String title) {
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }
    }

    public interface DocRepository extends JpaRepository<Doc, Long> {
    }

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(AuditingPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            DocRepository repo = ctx.getBean(DocRepository.class);
            auditingStampsTimestampsOnInsertAndUpdate(repo);
        }
    }

    static void auditingStampsTimestampsOnInsertAndUpdate(DocRepository repo) throws InterruptedException {
        System.out.println("--- @CreatedDate set on insert, @LastModifiedDate bumped on update ---");
        Doc saved = repo.saveAndFlush(new Doc("draft"));
        Instant created = saved.getCreatedAt();
        Instant firstUpdate = saved.getUpdatedAt();
        System.out.println("after insert: createdAt=" + created + " updatedAt=" + firstUpdate);

        Thread.sleep(1000);

        Doc reloaded = repo.findById(saved.getId()).orElseThrow();
        reloaded.setTitle("final");
        Doc updated = repo.saveAndFlush(reloaded);
        System.out.println("after update: createdAt=" + updated.getCreatedAt() + " updatedAt=" + updated.getUpdatedAt());

        System.out.println("createdAt unchanged = "
                + created.truncatedTo(ChronoUnit.MILLIS).equals(updated.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)));
        System.out.println("updatedAt advanced  = " + updated.getUpdatedAt().isAfter(firstUpdate));
    }
}
