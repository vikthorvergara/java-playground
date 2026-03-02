package com.github.vikthorvergara.reflection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationProcessingPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotNull {
        String message() default "must not be null";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Range {
        int min();
        int max();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Benchmark {
        int warmup() default 3;
        int iterations() default 5;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Entity {
        String table();
    }

    @Entity(table = "products")
    static class Product {
        @NotNull
        private String name;

        @NotNull(message = "category is required")
        private String category;

        @Range(min = 0, max = 10000)
        private int price;

        @Range(min = 0, max = 999)
        private int stock;

        Product(String name, String category, int price, int stock) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }
    }

    static class MathOps {
        @Benchmark(warmup = 2, iterations = 4)
        public long fibonacci(int n) {
            if (n <= 1) return n;
            long a = 0, b = 1;
            for (int i = 2; i <= n; i++) {
                long temp = b;
                b = a + b;
                a = temp;
            }
            return b;
        }

        @Benchmark
        public double sqrt(int value) {
            return Math.sqrt(value);
        }
    }

    static List<String> validate(Object obj) throws IllegalAccessException {
        var errors = new ArrayList<String>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);

            if (field.isAnnotationPresent(NotNull.class) && value == null) {
                var ann = field.getAnnotation(NotNull.class);
                errors.add(field.getName() + ": " + ann.message());
            }

            if (field.isAnnotationPresent(Range.class)) {
                var ann = field.getAnnotation(Range.class);
                if (value instanceof Integer intVal) {
                    if (intVal < ann.min() || intVal > ann.max()) {
                        errors.add(field.getName() + ": must be between " + ann.min() + " and " + ann.max());
                    }
                }
            }
        }
        return errors;
    }

    static void runBenchmarks(Object obj) throws Exception {
        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Benchmark.class)) continue;

            var ann = method.getAnnotation(Benchmark.class);
            System.out.printf("Benchmarking %s (warmup=%d, iterations=%d)%n",
                    method.getName(), ann.warmup(), ann.iterations());

            Object arg = Integer.valueOf(30);

            for (int i = 0; i < ann.warmup(); i++) {
                method.invoke(obj, arg);
            }

            long totalNanos = 0;
            Object result = null;
            for (int i = 0; i < ann.iterations(); i++) {
                long start = System.nanoTime();
                result = method.invoke(obj, arg);
                long elapsed = System.nanoTime() - start;
                totalNanos += elapsed;
                System.out.printf("  iteration %d: %d ns (result=%s)%n", i + 1, elapsed, result);
            }
            System.out.printf("  avg: %d ns%n%n", totalNanos / ann.iterations());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Entity Annotation ===");
        var entityAnn = Product.class.getAnnotation(Entity.class);
        System.out.println("Table: " + entityAnn.table());

        System.out.println("\n=== Validation: Valid Object ===");
        var valid = new Product("Laptop", "Electronics", 999, 50);
        var validErrors = validate(valid);
        System.out.println("Errors: " + (validErrors.isEmpty() ? "none" : validErrors));

        System.out.println("\n=== Validation: Invalid Object ===");
        var invalid = new Product(null, null, -5, 1500);
        var invalidErrors = validate(invalid);
        invalidErrors.forEach(e -> System.out.println("  - " + e));

        System.out.println("\n=== Annotation Metadata ===");
        for (Field field : Product.class.getDeclaredFields()) {
            var annotations = field.getDeclaredAnnotations();
            if (annotations.length > 0) {
                System.out.printf("  %s: %s%n", field.getName(),
                        java.util.Arrays.toString(annotations));
            }
        }

        System.out.println("\n=== Method Benchmarks ===");
        runBenchmarks(new MathOps());
    }
}
