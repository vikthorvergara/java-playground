package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@DisplayName("Parallel Execution Tests")
@Execution(ExecutionMode.CONCURRENT)
class ParallelExecutionPOCTest {

  private final ParallelExecutionPOC poc = new ParallelExecutionPOC();

  @Test
  @DisplayName("Parallel test 1")
  void testParallel1() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 1 on " + Thread.currentThread().getName());
    String result = poc.executeTask(1);
    assertNotNull(result);
    assertTrue(result.contains("Task 1"));
  }

  @Test
  @DisplayName("Parallel test 2")
  void testParallel2() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 2 on " + Thread.currentThread().getName());
    String result = poc.executeTask(2);
    assertNotNull(result);
    assertTrue(result.contains("Task 2"));
  }

  @Test
  @DisplayName("Parallel test 3")
  void testParallel3() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 3 on " + Thread.currentThread().getName());
    String result = poc.executeTask(3);
    assertNotNull(result);
    assertTrue(result.contains("Task 3"));
  }

  @Test
  @DisplayName("Parallel test 4")
  void testParallel4() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 4 on " + Thread.currentThread().getName());
    assertEquals(16, poc.compute(4));
  }

  @Test
  @DisplayName("Parallel test 5")
  void testParallel5() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 5 on " + Thread.currentThread().getName());
    assertEquals(25, poc.compute(5));
  }

  @Test
  @DisplayName("Parallel test 6")
  void testParallel6() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 6 on " + Thread.currentThread().getName());
    assertTrue(poc.process("test"));
  }

  @Test
  @DisplayName("Parallel test 7")
  void testParallel7() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 7 on " + Thread.currentThread().getName());
    assertFalse(poc.process(""));
  }

  @Test
  @DisplayName("Parallel test 8")
  void testParallel8() throws InterruptedException {
    Thread.sleep(100);
    System.out.println("Test 8 on " + Thread.currentThread().getName());
    assertFalse(poc.process(null));
  }

  @Test
  @Execution(ExecutionMode.SAME_THREAD)
  @DisplayName("Sequential test in parallel class")
  void testSequential() {
    System.out.println("Sequential test on " + Thread.currentThread().getName());
    assertNotNull(poc.executeTask(99));
  }
}
