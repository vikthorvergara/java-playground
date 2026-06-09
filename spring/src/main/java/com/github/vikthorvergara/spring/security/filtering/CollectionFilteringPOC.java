package com.github.vikthorvergara.spring.security.filtering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableMethodSecurity
public class CollectionFilteringPOC {

    public record Note(String owner, String text) {
    }

    @Service
    public static class NoteService {
        private final List<Note> store = new ArrayList<>();

        void seed(Note note) {
            store.add(note);
        }

        @PostFilter("filterObject.owner == authentication.name")
        public List<Note> findAll() {
            return new ArrayList<>(store);
        }

        @PreFilter("filterObject.owner == authentication.name")
        public List<Note> saveAll(List<Note> incoming) {
            store.addAll(incoming);
            return incoming;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CollectionFilteringPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            NoteService notes = ctx.getBean(NoteService.class);
            notes.seed(new Note("alice", "alice one"));
            notes.seed(new Note("bob", "bob one"));
            notes.seed(new Note("alice", "alice two"));

            authenticateAs("alice");
            postFilterReturnsOnlyOwnedRows(notes);
            preFilterDropsForeignRowsBeforeMethodRuns(notes);
            postFilterReflectsOnlyOwnedAfterSave(notes);
        }
    }

    static void authenticateAs(String name) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(name, "n", List.of()));
    }

    static void postFilterReturnsOnlyOwnedRows(NoteService notes) {
        System.out.println("--- @PostFilter: alice sees only her notes ---");
        notes.findAll().forEach(n -> System.out.println("  " + n));
    }

    static void preFilterDropsForeignRowsBeforeMethodRuns(NoteService notes) {
        System.out.println("\n--- @PreFilter: alice submits mixed batch, bob's row dropped before save ---");
        List<Note> batch = new ArrayList<>(List.of(
                new Note("alice", "alice three"),
                new Note("bob", "bob smuggled")));
        List<Note> accepted = notes.saveAll(batch);
        accepted.forEach(n -> System.out.println("  accepted " + n));
    }

    static void postFilterReflectsOnlyOwnedAfterSave(NoteService notes) {
        System.out.println("\n--- @PostFilter again: smuggled bob row never stored under alice ---");
        notes.findAll().forEach(n -> System.out.println("  " + n));
    }
}
