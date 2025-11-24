package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.*;

@DisplayName("JUnit Lifecycle Methods")
class LifecyclePOCTest {

  private static int testCounter;
  private LifecyclePOC poc;

  @BeforeAll
  static void setupAll() {
    testCounter = 0;
    System.out.println("@BeforeAll: Executed once before all tests");
  }

  @BeforeEach
  void setUp() {
    poc = new LifecyclePOC();
    poc.setItems(new ArrayList<>());
    testCounter++;
    System.out.println("@BeforeEach: Setting up test #" + testCounter);
  }

  @Test
  @DisplayName("Test adding items")
  void testAddItem() {
    System.out.println("  Executing: testAddItem");
    poc.addItem("Item1");
    poc.addItem("Item2");

    assertEquals(2, poc.getItemCount());
    assertTrue(poc.getItems().contains("Item1"));
    assertTrue(poc.getItems().contains("Item2"));
  }

  @Test
  @DisplayName("Test removing items")
  void testRemoveItem() {
    System.out.println("  Executing: testRemoveItem");
    poc.addItem("Item1");
    poc.addItem("Item2");
    poc.removeItem("Item1");

    assertEquals(1, poc.getItemCount());
    assertFalse(poc.getItems().contains("Item1"));
    assertTrue(poc.getItems().contains("Item2"));
  }

  @Test
  @DisplayName("Test clearing all items")
  void testClear() {
    System.out.println("  Executing: testClear");
    poc.addItem("Item1");
    poc.addItem("Item2");
    poc.addItem("Item3");

    assertEquals(3, poc.getItemCount());

    poc.clear();
    assertEquals(0, poc.getItemCount());
    assertTrue(poc.getItems().isEmpty());
  }

  @Test
  @DisplayName("Test getting items returns copy")
  void testGetItemsReturnsCopy() {
    System.out.println("  Executing: testGetItemsReturnsCopy");
    poc.addItem("Item1");
    var items = poc.getItems();
    items.add("Item2");

    assertEquals(1, poc.getItemCount());
  }

  @AfterEach
  void tearDown() {
    System.out.println("@AfterEach: Cleaning up after test #" + testCounter);
    poc.clear();
    poc = null;
  }

  @AfterAll
  static void tearDownAll() {
    System.out.println("@AfterAll: Executed once after all tests");
    System.out.println("Total tests executed: " + testCounter);
  }
}
