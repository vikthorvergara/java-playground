package com.github.vikthorvergara.spring.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RestSupportAndRestTemplatePOC.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate rest;
    String base;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        base = "http://localhost:" + port;
    }

    @Test
    @Order(1)
    void listIsInitiallyEmpty() {
        ResponseEntity<List<RestSupportAndRestTemplatePOC.Book>> res = rest.exchange(
                base + "/api/books", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RestSupportAndRestTemplatePOC.Book>>() {});
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(0, res.getBody().size());
    }

    @Test
    @Order(2)
    void postCreatesBookWithGeneratedId() {
        var input = new RestSupportAndRestTemplatePOC.Book(null, "Refactoring", "Martin Fowler");
        ResponseEntity<RestSupportAndRestTemplatePOC.Book> res = rest.postForEntity(
                base + "/api/books", input, RestSupportAndRestTemplatePOC.Book.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1L, res.getBody().id());
        assertEquals("Martin Fowler", res.getBody().author());
    }

    @Test
    @Order(3)
    void getByIdReturnsCreatedBook() {
        ResponseEntity<RestSupportAndRestTemplatePOC.Book> res = rest.getForEntity(
                base + "/api/books/{id}", RestSupportAndRestTemplatePOC.Book.class, 1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("Refactoring", res.getBody().title());
    }

    @Test
    @Order(4)
    void filterByAuthorMatches() {
        rest.postForEntity(base + "/api/books",
                new RestSupportAndRestTemplatePOC.Book(null, "Domain Driven Design", "Eric Evans"),
                RestSupportAndRestTemplatePOC.Book.class);
        ResponseEntity<List<RestSupportAndRestTemplatePOC.Book>> res = rest.exchange(
                base + "/api/books?author={a}", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RestSupportAndRestTemplatePOC.Book>>() {},
                "Eric Evans");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
        assertEquals("Domain Driven Design", res.getBody().get(0).title());
    }

    @Test
    @Order(5)
    void deleteRemovesBook() {
        ResponseEntity<Map> res = rest.exchange(
                base + "/api/books/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Map.class, 1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue((Boolean) res.getBody().get("removed"));
    }
}
