package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Test Tagging and Filtering")
class TaggingPOCTest {

  private final TaggingPOC poc = new TaggingPOC();

  @Test
  @Tag("fast")
  @DisplayName("Fast test that runs quickly")
  void testFastOperation() {
    assertEquals("fast-result", poc.fastOperation());
  }

  @Test
  @Tag("slow")
  @DisplayName("Slow test that takes time")
  void testSlowOperation() throws InterruptedException {
    assertEquals("slow-result", poc.slowOperation());
  }

  @Test
  @Tag("unit")
  @Tag("fast")
  @DisplayName("Unit test with multiple tags")
  void testMultipleTags() {
    assertNotNull(poc.fastOperation());
  }

  @Test
  @Tag("integration")
  @Tag("database")
  @DisplayName("Database integration test")
  void testDatabaseQuery() {
    String result = poc.queryDatabase("SELECT * FROM users");
    assertTrue(result.contains("Query result"));
  }

  @Test
  @Tag("integration")
  @Tag("api")
  @DisplayName("External API integration test")
  void testExternalAPI() {
    assertEquals("API response", poc.callExternalAPI());
  }

  @Test
  @Tag("user")
  @Tag("crud")
  @DisplayName("Create user operation")
  void testCreateUser() {
    String result = poc.createUser("Vikthor");
    assertTrue(result.contains("Vikthor"));
  }

  @Test
  @Tag("user")
  @Tag("crud")
  @DisplayName("Update user operation")
  void testUpdateUser() {
    String result = poc.updateUser(1, "Prateek");
    assertTrue(result.contains("Prateek"));
  }

  @Test
  @Tag("user")
  @Tag("crud")
  @DisplayName("Delete user operation")
  void testDeleteUser() {
    String result = poc.deleteUser(1);
    assertTrue(result.contains("deleted"));
  }

  @Test
  @Tag("production")
  @DisplayName("Production readiness check")
  void testProductionReady() {
    assertTrue(poc.isProductionReady());
  }

  @Test
  @Tag("development")
  @DisplayName("Development feature test")
  void testDevelopmentFeature() {
    assertEquals("dev-feature", poc.developmentFeature());
  }

  @Test
  @Tag("smoke")
  @Tag("fast")
  @DisplayName("Smoke test for basic functionality")
  void testBasicSmoke() {
    assertNotNull(poc.fastOperation());
    assertNotNull(poc.createUser("Test"));
  }

  @Test
  @Tag("regression")
  @DisplayName("Regression test")
  void testRegression() {
    assertEquals("fast-result", poc.fastOperation());
    assertEquals("User Andrei created", poc.createUser("Andrei"));
  }

  @Test
  @Tag("acceptance")
  @Tag("user")
  @DisplayName("Acceptance test for user workflow")
  void testUserWorkflow() {
    String create = poc.createUser("Santokh");
    String update = poc.updateUser(1, "Santokh Updated");
    String delete = poc.deleteUser(1);

    assertAll(
        () -> assertTrue(create.contains("Santokh")),
        () -> assertTrue(update.contains("updated")),
        () -> assertTrue(delete.contains("deleted")));
  }
}
