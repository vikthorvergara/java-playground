package com.github.vikthorvergara.spring.core.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = WebTestClientIT.TestApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class WebTestClientIT {

    @SpringBootApplication
    static class TestApp {
        @Bean
        SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .csrf(c -> c.disable())
                    .build();
        }

        @Bean
        GreetController greetController() {
            return new GreetController();
        }
    }

    @RestController
    static class GreetController {
        @Value("${app.name:fallback}")
        private String appName;

        @GetMapping("/greet/{who}")
        public String greet(@PathVariable String who) {
            return appName + " greets " + who;
        }
    }

    @LocalServerPort
    int port;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void greetsByPathVariable() {
        client.get().uri("/greet/{who}", "Vik")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("spring-poc greets Vik");
    }

    @Test
    void rejectsMissingPathSegment() {
        client.get().uri("/greet/")
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
