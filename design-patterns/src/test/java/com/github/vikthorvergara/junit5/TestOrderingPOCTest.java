package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

@DisplayName("Test Ordering Strategies")
class TestOrderingPOCTest {

  @Nested
  @DisplayName("Alphanumeric Order")
  @TestMethodOrder(MethodOrderer.MethodName.class)
  class AlphanumericOrder {

    @Test
    void test_C_Third() {
      System.out.println("Executing test_C_Third");
      assertTrue(true);
    }

    @Test
    void test_A_First() {
      System.out.println("Executing test_A_First");
      assertTrue(true);
    }

    @Test
    void test_B_Second() {
      System.out.println("Executing test_B_Second");
      assertTrue(true);
    }
  }

  @Nested
  @DisplayName("Order Annotation")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class OrderAnnotationTests {

    @Test
    @Order(3)
    void shouldExecuteThird() {
      System.out.println("Executing third");
      assertTrue(true);
    }

    @Test
    @Order(1)
    void shouldExecuteFirst() {
      System.out.println("Executing first");
      assertTrue(true);
    }

    @Test
    @Order(2)
    void shouldExecuteSecond() {
      System.out.println("Executing second");
      assertTrue(true);
    }

    @Test
    @Order(4)
    void shouldExecuteFourth() {
      System.out.println("Executing fourth");
      assertTrue(true);
    }
  }

  @Nested
  @DisplayName("DisplayName Order")
  @TestMethodOrder(MethodOrderer.DisplayName.class)
  class DisplayNameOrder {

    @Test
    @DisplayName("C - Third test")
    void test1() {
      System.out.println("Executing C - Third test");
      assertTrue(true);
    }

    @Test
    @DisplayName("A - First test")
    void test2() {
      System.out.println("Executing A - First test");
      assertTrue(true);
    }

    @Test
    @DisplayName("B - Second test")
    void test3() {
      System.out.println("Executing B - Second test");
      assertTrue(true);
    }
  }

  @Nested
  @DisplayName("Random Order")
  @TestMethodOrder(MethodOrderer.Random.class)
  class RandomOrder {

    @Test
    void testOne() {
      System.out.println("Executing testOne");
      assertTrue(true);
    }

    @Test
    void testTwo() {
      System.out.println("Executing testTwo");
      assertTrue(true);
    }

    @Test
    void testThree() {
      System.out.println("Executing testThree");
      assertTrue(true);
    }

    @Test
    void testFour() {
      System.out.println("Executing testFour");
      assertTrue(true);
    }
  }

  @Nested
  @DisplayName("Integration Tests with Order")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class IntegrationTestsWithOrder {

    private final TestOrderingPOC poc = new TestOrderingPOC();

    @Test
    @Order(1)
    @DisplayName("Step 1: Initialize counter")
    void initializeCounter() {
      System.out.println("Step 1: Initializing");
      poc.setValue(0);
      assertEquals(0, poc.getValue());
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: Increment counter")
    void incrementCounter() {
      System.out.println("Step 2: Incrementing");
      poc.increment();
      assertEquals(1, poc.getValue());
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: Verify positive")
    void verifyPositive() {
      System.out.println("Step 3: Verifying positive");
      assertTrue(poc.isPositive());
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Reset counter")
    void resetCounter() {
      System.out.println("Step 4: Resetting");
      poc.reset();
      assertTrue(poc.isZero());
    }
  }
}
