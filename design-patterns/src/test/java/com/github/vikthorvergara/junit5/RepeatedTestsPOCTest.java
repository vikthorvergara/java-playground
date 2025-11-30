package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

@DisplayName("Repeated Tests")
class RepeatedTestsPOCTest {

  @RepeatedTest(5)
  @DisplayName("Simple repeated test")
  void testRepeatedBasic() {
    RepeatedTestsPOC poc = new RepeatedTestsPOC(42);
    int result = poc.generateRandomNumber();
    assertTrue(result >= 0 && result < 100);
  }

  @RepeatedTest(value = 10, name = "Repetition {currentRepetition} of {totalRepetitions}")
  @DisplayName("Repeated test with custom display name")
  void testRepeatedWithCustomName() {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();
    assertTrue(poc.isStable());
  }

  @RepeatedTest(3)
  @DisplayName("Repeated test with RepetitionInfo")
  void testWithRepetitionInfo(RepetitionInfo repetitionInfo) {
    System.out.println(
        "Executing repetition "
            + repetitionInfo.getCurrentRepetition()
            + " of "
            + repetitionInfo.getTotalRepetitions());

    RepeatedTestsPOC poc = new RepeatedTestsPOC();
    assertNotNull(poc.processIteration(repetitionInfo.getCurrentRepetition()));
  }

  @RepeatedTest(5)
  @DisplayName("Test repetition behavior with counter")
  void testWithCounter(RepetitionInfo info) {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();
    int current = info.getCurrentRepetition();
    int total = info.getTotalRepetitions();

    assertTrue(current >= 1 && current <= total);
    assertEquals(5, total);
  }

  @RepeatedTest(value = 4, name = "{displayName} - iteration {currentRepetition}/{totalRepetitions}")
  @DisplayName("Calculate sum repeatedly")
  void testCalculateSumRepeatedly(RepetitionInfo info) {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();
    int n = info.getCurrentRepetition();
    int sum = poc.calculateSum(n);

    int expected = n * (n + 1) / 2;
    assertEquals(expected, sum);
  }

  @RepeatedTest(value = 10, name = "Stability test #{currentRepetition}")
  @DisplayName("Test stability across multiple runs")
  void testStability() {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();
    assertTrue(poc.isStable());
  }

  @RepeatedTest(value = 3, name = "Run {currentRepetition}: {displayName}")
  @DisplayName("Different behavior per repetition")
  void testDifferentBehavior(RepetitionInfo info) {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();

    switch (info.getCurrentRepetition()) {
      case 1:
        System.out.println("First repetition");
        assertEquals(0, poc.calculateSum(0));
        break;
      case 2:
        System.out.println("Second repetition");
        assertEquals(1, poc.calculateSum(1));
        break;
      case 3:
        System.out.println("Third repetition");
        assertEquals(6, poc.calculateSum(3));
        break;
    }
  }

  @RepeatedTest(value = 20, name = "Connection validation attempt {currentRepetition}")
  @DisplayName("Test connection validation with retries")
  void testConnectionValidation(RepetitionInfo info) {
    RepeatedTestsPOC poc = new RepeatedTestsPOC();

    boolean isConnected = poc.validateConnection();

    System.out.println(
        "Attempt "
            + info.getCurrentRepetition()
            + ": "
            + (isConnected ? "Connected" : "Failed"));
  }
}
