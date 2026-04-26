package com.github.vikthorvergara.spring.web.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class RestSupportAndRestTemplatePOC {

    public record Book(Long id, String title, String author) {
    }

    @RestController
    @RequestMapping("/api/books")
    public static class BookController {
        private final Map<Long, Book> store = new LinkedHashMap<>();
        private long seq = 0L;

        @GetMapping
        public List<Book> list(@RequestParam(required = false) String author) {
            if (author == null) {
                return store.values().stream().toList();
            }
            return store.values().stream().filter(b -> b.author().equalsIgnoreCase(author)).toList();
        }

        @GetMapping("/{id}")
        public Book one(@PathVariable Long id) {
            return store.get(id);
        }

        @PostMapping
        public Book create(@RequestBody Book in) {
            long id = ++seq;
            Book b = new Book(id, in.title(), in.author());
            store.put(id, b);
            return b;
        }

        @PutMapping("/{id}")
        public Book replace(@PathVariable Long id, @RequestBody Book in) {
            Book b = new Book(id, in.title(), in.author());
            store.put(id, b);
            return b;
        }

        @DeleteMapping("/{id}")
        public Map<String, Object> remove(@PathVariable Long id) {
            Book gone = store.remove(id);
            return Map.of("removed", gone != null, "id", id);
        }
    }

    @Bean
    SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .csrf(c -> c.disable())
                .build();
    }

    @Bean
    PortHolder portHolder() {
        return new PortHolder();
    }

    public static class PortHolder implements ApplicationListener<WebServerInitializedEvent> {
        volatile int port;

        @Override
        public void onApplicationEvent(WebServerInitializedEvent event) {
            this.port = event.getWebServer().getPort();
        }
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(RestSupportAndRestTemplatePOC.class,
                "--server.port=0", "--logging.level.org.springframework.web=WARN")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port + "/api/books";
            RestTemplate rt = new RestTemplate();

            createTwo(rt, base);
            getOne(rt, base);
            listAll(rt, base);
            queryByAuthor(rt, base);
            replaceOne(rt, base);
            deleteOne(rt, base);
            listAfterDelete(rt, base);
        }
    }

    static void createTwo(RestTemplate rt, String base) {
        System.out.println("--- POST create ---");
        Book a = rt.postForObject(base, new Book(null, "Effective Java", "Joshua Bloch"), Book.class);
        Book b = rt.postForObject(base, new Book(null, "Clean Code", "Robert Martin"), Book.class);
        System.out.println("created: " + a);
        System.out.println("created: " + b);
    }

    static void getOne(RestTemplate rt, String base) {
        System.out.println("\n--- GET by id ---");
        Book b = rt.getForObject(base + "/{id}", Book.class, 1L);
        System.out.println("id=1 -> " + b);
    }

    static void listAll(RestTemplate rt, String base) {
        System.out.println("\n--- GET list ---");
        ResponseEntity<List<Book>> res = rt.exchange(
                base, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Book>>() {});
        System.out.println("status: " + res.getStatusCode());
        res.getBody().forEach(System.out::println);
    }

    static void queryByAuthor(RestTemplate rt, String base) {
        System.out.println("\n--- GET ?author= ---");
        Book[] arr = rt.getForObject(base + "?author={a}", Book[].class, "Joshua Bloch");
        for (Book b : arr) {
            System.out.println(b);
        }
    }

    static void replaceOne(RestTemplate rt, String base) {
        System.out.println("\n--- PUT replace ---");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Book> entity = new HttpEntity<>(new Book(null, "Effective Java 3rd", "Joshua Bloch"), headers);
        Book updated = rt.exchange(base + "/{id}", HttpMethod.PUT, entity, Book.class, 1L).getBody();
        System.out.println("updated: " + updated);
    }

    static void deleteOne(RestTemplate rt, String base) {
        System.out.println("\n--- DELETE ---");
        ResponseEntity<Map> res = rt.exchange(base + "/{id}", HttpMethod.DELETE, null, Map.class, 2L);
        System.out.println("status: " + res.getStatusCode() + " body: " + res.getBody());
    }

    static void listAfterDelete(RestTemplate rt, String base) {
        System.out.println("\n--- GET list after delete ---");
        Book[] arr = rt.getForObject(base, Book[].class);
        for (Book b : arr) {
            System.out.println(b);
        }
    }
}
