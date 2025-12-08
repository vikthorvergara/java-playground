package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

@DisplayName("Custom Extensions")
class CustomExtensionPOCTest {

  @Test
  @ExtendWith(TimingExtension.class)
  @DisplayName("Test with timing extension")
  void testWithTiming() throws InterruptedException {
    CustomExtensionPOC poc = new CustomExtensionPOC();
    Thread.sleep(100);
    assertEquals("Processed: test", poc.process("test"));
  }

  @Test
  @ExtendWith(LoggingExtension.class)
  @DisplayName("Test with logging extension")
  void testWithLogging() {
    CustomExtensionPOC poc = new CustomExtensionPOC();
    poc.setData("test-data");
    assertEquals("test-data", poc.getData());
  }

  @Test
  @ExtendWith({TimingExtension.class, LoggingExtension.class})
  @DisplayName("Test with multiple extensions")
  void testWithMultipleExtensions() {
    CustomExtensionPOC poc = new CustomExtensionPOC();
    int result = poc.calculate(10);
    assertEquals(20, result);
  }

  @Test
  @Timed
  @DisplayName("Test with custom annotation")
  void testWithCustomAnnotation() throws InterruptedException {
    CustomExtensionPOC poc = new CustomExtensionPOC();
    Thread.sleep(50);
    assertNotNull(poc.process("input"));
  }

  @Test
  @ExtendWith(RandomNumberExtension.class)
  @DisplayName("Test with parameter resolver")
  void testWithParameterResolver(int randomNumber) {
    System.out.println("Random number: " + randomNumber);
    assertTrue(randomNumber >= 0 && randomNumber < 100);
  }

  @Test
  @ExtendWith(ConditionalExtension.class)
  @DisplayName("Test with conditional execution extension")
  void testWithConditionalExtension() {
    CustomExtensionPOC poc = new CustomExtensionPOC();
    assertTrue(true);
  }

  static class TimingExtension implements BeforeEachCallback, AfterEachCallback {

    private long startTime;

    @Override
    public void beforeEach(ExtensionContext context) {
      startTime = System.currentTimeMillis();
      System.out.println("Starting test: " + context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) {
      long duration = System.currentTimeMillis() - startTime;
      System.out.println("Test completed in " + duration + "ms");
    }
  }

  static class LoggingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
      System.out.println("BEFORE: " + context.getTestMethod().get().getName());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
      System.out.println("AFTER: " + context.getTestMethod().get().getName());
    }
  }

  static class RandomNumberExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext, ExtensionContext extensionContext) {
      return parameterContext.getParameter().getType() == int.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext, ExtensionContext extensionContext) {
      return (int) (Math.random() * 100);
    }
  }

  static class ConditionalExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
      boolean shouldRun = true;
      if (shouldRun) {
        return ConditionEvaluationResult.enabled("Test is enabled");
      } else {
        return ConditionEvaluationResult.disabled("Test is disabled");
      }
    }
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ExtendWith(TimingExtension.class)
  @interface Timed {}
}
