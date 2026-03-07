package com.github.vikthorvergara.annotations.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MiniTestFrameworkPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Test {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface BeforeEach {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Disabled {
        String reason();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ShouldThrow {
        Class<? extends Throwable> value();
    }

    static class TestResult {
        final String methodName;
        final String status;
        final String detail;

        TestResult(String methodName, String status, String detail) {
            this.methodName = methodName;
            this.status = status;
            this.detail = detail;
        }
    }

    static class TestRunner {

        static List<TestResult> run(Class<?> testClass) {
            var results = new ArrayList<TestResult>();
            var testMethods = new ArrayList<Method>();
            var beforeEachMethods = new ArrayList<Method>();

            for (var method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    testMethods.add(method);
                }
                if (method.isAnnotationPresent(BeforeEach.class)) {
                    beforeEachMethods.add(method);
                }
            }

            Object instance;
            try {
                var constructor = testClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                instance = constructor.newInstance();
            } catch (Exception e) {
                System.out.println("FATAL: Could not instantiate " + testClass.getSimpleName());
                return results;
            }

            for (var testMethod : testMethods) {
                testMethod.setAccessible(true);

                if (testMethod.isAnnotationPresent(Disabled.class)) {
                    var annotation = testMethod.getAnnotation(Disabled.class);
                    results.add(new TestResult(testMethod.getName(), "SKIPPED", annotation.reason()));
                    continue;
                }

                for (var beforeEach : beforeEachMethods) {
                    try {
                        beforeEach.setAccessible(true);
                        beforeEach.invoke(instance);
                    } catch (Exception e) {
                        results.add(new TestResult(testMethod.getName(), "FAIL", "BeforeEach failed: " + e.getCause()));
                        continue;
                    }
                }

                var shouldThrow = testMethod.getAnnotation(ShouldThrow.class);

                try {
                    testMethod.invoke(instance);
                    if (shouldThrow != null) {
                        results.add(new TestResult(testMethod.getName(), "FAIL",
                                "Expected " + shouldThrow.value().getSimpleName() + " but nothing was thrown"));
                    } else {
                        results.add(new TestResult(testMethod.getName(), "PASS", ""));
                    }
                } catch (Exception e) {
                    var cause = e.getCause() != null ? e.getCause() : e;

                    if (shouldThrow != null && shouldThrow.value().isInstance(cause)) {
                        results.add(new TestResult(testMethod.getName(), "PASS",
                                "Caught expected " + cause.getClass().getSimpleName()));
                    } else if (cause instanceof AssertionError) {
                        results.add(new TestResult(testMethod.getName(), "FAIL", cause.getMessage()));
                    } else {
                        results.add(new TestResult(testMethod.getName(), "FAIL",
                                cause.getClass().getSimpleName() + ": " + cause.getMessage()));
                    }
                }
            }

            return results;
        }

        static void printResults(List<TestResult> results) {
            System.out.println("--- Test Results ---");
            int passed = 0, failed = 0, skipped = 0;

            for (var result : results) {
                var detail = result.detail.isEmpty() ? "" : " (" + result.detail + ")";
                System.out.printf("  [%s] %s%s%n", result.status, result.methodName, detail);

                switch (result.status) {
                    case "PASS" -> passed++;
                    case "FAIL" -> failed++;
                    case "SKIPPED" -> skipped++;
                }
            }

            System.out.println("---");
            System.out.printf("Total: %d | Passed: %d | Failed: %d | Skipped: %d%n",
                    results.size(), passed, failed, skipped);
            System.out.println(failed == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED");
        }
    }

    static class CalculatorTests {

        private int accumulator;

        @BeforeEach
        void resetState() {
            accumulator = 0;
        }

        @Test
        void additionShouldWork() {
            accumulator = 10 + 5;
            assertEqual(15, accumulator, "10 + 5 should be 15");
        }

        @Test
        void subtractionShouldFail() {
            accumulator = 10 - 3;
            assertEqual(99, accumulator, "intentionally wrong assertion");
        }

        @Test
        @Disabled(reason = "multiplication logic is under revision")
        void multiplicationSkipped() {
            accumulator = 4 * 5;
            assertEqual(20, accumulator, "4 * 5 should be 20");
        }

        @Test
        @ShouldThrow(ArithmeticException.class)
        void divisionByZeroShouldThrow() {
            @SuppressWarnings("unused")
            var result = 1 / 0;
        }

        @Test
        void moduloShouldWork() {
            accumulator = 17 % 5;
            assertEqual(2, accumulator, "17 % 5 should be 2");
        }

        private static void assertEqual(int expected, int actual, String message) {
            if (expected != actual) {
                throw new AssertionError(message + " [expected=" + expected + ", actual=" + actual + "]");
            }
        }
    }

    static class AssertionError extends RuntimeException {
        AssertionError(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Mini Test Framework ===\n");
        System.out.println("Running: " + CalculatorTests.class.getSimpleName() + "\n");

        var results = TestRunner.run(CalculatorTests.class);
        TestRunner.printResults(results);
    }
}
