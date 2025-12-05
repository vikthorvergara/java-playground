package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

@DisplayName("Test Instance Lifecycle")
class TestInstancePOCTest {

  @Nested
  @DisplayName("PER_METHOD Lifecycle (default)")
  class PerMethodLifecycle {

    private final TestInstancePOC poc = new TestInstancePOC();
    private static int testExecutionCount = 0;

    @Test
    @DisplayName("First test with PER_METHOD")
    void testFirst() {
      testExecutionCount++;
      System.out.println("PER_METHOD Test 1 - execution count: " + testExecutionCount);
      poc.increment();
      assertEquals(1, poc.getCounter());
    }

    @Test
    @DisplayName("Second test with PER_METHOD")
    void testSecond() {
      testExecutionCount++;
      System.out.println("PER_METHOD Test 2 - execution count: " + testExecutionCount);
      poc.increment();
      assertEquals(1, poc.getCounter());
    }

    @Test
    @DisplayName("Third test with PER_METHOD")
    void testThird() {
      testExecutionCount++;
      System.out.println("PER_METHOD Test 3 - execution count: " + testExecutionCount);
      poc.increment();
      assertEquals(1, poc.getCounter());
    }
  }

  @Nested
  @DisplayName("PER_CLASS Lifecycle")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class PerClassLifecycle {

    private final TestInstancePOC poc = new TestInstancePOC();
    private int testExecutionCount = 0;

    @BeforeAll
    void setupAll() {
      System.out.println("PER_CLASS: @BeforeAll can be non-static");
      poc.setCounter(0);
    }

    @Test
    @Order(1)
    @DisplayName("First test with PER_CLASS")
    void testFirst() {
      testExecutionCount++;
      System.out.println("PER_CLASS Test 1 - execution count: " + testExecutionCount);
      poc.increment();
      assertEquals(1, poc.getCounter());
    }

    @Test
    @Order(2)
    @DisplayName("Second test with PER_CLASS")
    void testSecond() {
      testExecutionCount++;
      System.out.println("PER_CLASS Test 2 - execution count: " + testExecutionCount);
      poc.increment();
      assertEquals(2, poc.getCounter());
    }

    @Test
    @Order(3)
    @DisplayName("Third test with PER_CLASS")
    void testThird() {
      testExecutionCount++;
      System.out.println("PER_CLASS Test 3 - execution count: " + testExecutionCount);
      poc.increment();
      assertEquals(3, poc.getCounter());
    }

    @AfterAll
    void tearDownAll() {
      System.out.println("PER_CLASS: @AfterAll can be non-static");
      System.out.println("Final counter value: " + poc.getCounter());
      System.out.println("Total tests executed: " + testExecutionCount);
    }
  }

  @Nested
  @DisplayName("PER_CLASS with shared state")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class SharedStateTests {

    private final TestInstancePOC poc = new TestInstancePOC();

    @Test
    @Order(1)
    @DisplayName("Initialize state")
    void initialize() {
      poc.setCounter(10);
      assertEquals(10, poc.getCounter());
    }

    @Test
    @Order(2)
    @DisplayName("Modify state")
    void modify() {
      poc.increment();
      poc.increment();
      assertEquals(12, poc.getCounter());
    }

    @Test
    @Order(3)
    @DisplayName("Verify final state")
    void verify() {
      assertEquals(12, poc.getCounter());
    }
  }
}
